package com.scheduler.core;

import com.scheduler.events.EventBus;
import com.scheduler.events.TaskScheduledEvent;
import com.scheduler.execution.TaskExecutor;
import com.scheduler.factory.JobFactory;
import com.scheduler.persistence.JobRepository;
import com.scheduler.tasks.Task;
import com.scheduler.tasks.TaskCommand;
import com.scheduler.triggers.Trigger;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SchedulerEngine {

    private final SchedulerConfig config;
    private final EventBus eventBus;
    private final TaskExecutor taskExecutor;
    private final JobFactory jobFactory;
    private final JobRepository jobRepository;
    private final PriorityBlockingQueue<ScheduledJob> queue = new PriorityBlockingQueue<>();
    private final Map<String, ScheduledJob> jobs = new ConcurrentHashMap<>();
    private final Object orchestratorMonitor = new Object();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread orchestratorThread;

    public SchedulerEngine(SchedulerConfig config, EventBus eventBus, TaskExecutor taskExecutor,
                    JobFactory jobFactory, JobRepository jobRepository) {
        this.config = Objects.requireNonNull(config);
        this.eventBus = Objects.requireNonNull(eventBus);
        this.taskExecutor = Objects.requireNonNull(taskExecutor);
        this.jobFactory = Objects.requireNonNull(jobFactory);
        this.jobRepository = Objects.requireNonNull(jobRepository);
    }

    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        orchestratorThread = new Thread(this::orchestratorLoop, "scheduler-orchestrator");
        orchestratorThread.setDaemon(true);
        orchestratorThread.start();
    }

    public void shutdown() {
        running.set(false);
        synchronized (orchestratorMonitor) {
            orchestratorMonitor.notifyAll();
        }
        if (orchestratorThread != null) {
            try {
                orchestratorThread.join(5000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        taskExecutor.shutdown();
        try {
            taskExecutor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public void shutdownNow() {
        running.set(false);
        synchronized (orchestratorMonitor) {
            orchestratorMonitor.notifyAll();
        }
        taskExecutor.shutdownNow();
    }

    public String schedule(Task task, Trigger trigger, com.scheduler.retry.RetryPolicy retryPolicy, Instant startTime) {
        ScheduledJob job = jobFactory.createScheduledJob(task, trigger, retryPolicy, startTime);
        registerJob(job);
        return job.getJobId();
    }

    public String schedule(JobBuilder.JobDefinition definition) {
        return schedule(definition.task(), definition.trigger(), definition.retryPolicy(), definition.startTime());
    }

    public boolean cancel(String jobId) {
        ScheduledJob job = jobs.remove(jobId);
        if (job == null) {
            return false;
        }
        queue.remove(job);
        job.setState(JobState.CANCELLED);
        jobRepository.delete(jobId);
        wakeOrchestrator();
        return true;
    }

    public boolean pause(String jobId) {
        ScheduledJob job = jobs.get(jobId);
        if (job == null || job.getState() == JobState.CANCELLED) {
            return false;
        }
        queue.remove(job);
        job.setState(JobState.PAUSED);
        jobRepository.save(job);
        return true;
    }

    public boolean resume(String jobId) {
        ScheduledJob job = jobs.get(jobId);
        if (job == null || job.getState() != JobState.PAUSED) {
            return false;
        }
        job.setState(JobState.SCHEDULED);
        queue.offer(job);
        jobRepository.save(job);
        wakeOrchestrator();
        return true;
    }

    public Optional<ScheduledJob> findJob(String jobId) {
        return Optional.ofNullable(jobs.get(jobId));
    }

    private void registerJob(ScheduledJob job) {
        jobs.put(job.getJobId(), job);
        jobRepository.save(job);
        queue.offer(job);
        eventBus.publish(new TaskScheduledEvent(
                job.getJobId(),
                job.getTask().getId(),
                job.getNextFireTime(),
                Instant.now()
        ));
        wakeOrchestrator();
    }

    private void orchestratorLoop() {
        while (running.get()) {
            try {
                ScheduledJob job = queue.peek();
                if (job == null || job.getState() != JobState.SCHEDULED) {
                    waitForSignal(config.getOrchestratorTickMs());
                    continue;
                }
                Instant now = Instant.now();
                if (job.getNextFireTime().isAfter(now)) {
                    long waitMs = Math.min(
                            job.getNextFireTime().toEpochMilli() - now.toEpochMilli(),
                            config.getOrchestratorTickMs()
                    );
                    waitForSignal(Math.max(waitMs, 1));
                    continue;
                }
                queue.poll();
                if (job.getState() != JobState.SCHEDULED) {
                    continue;
                }
                dispatchJob(job, now);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void dispatchJob(ScheduledJob job, Instant fireTime) {
        TriggerContext firedContext = job.getTriggerContext().withFire(fireTime);
        job.setTriggerContext(firedContext);
        job.setState(JobState.RUNNING);
        jobRepository.save(job);

        Task decoratedTask = jobFactory.createDecoratedTask(job);
        TaskCommand command = new TaskCommand(job, decoratedTask, eventBus, fireTime, this::handleCompletion);
        taskExecutor.submit(command);
    }

    private void handleCompletion(ScheduledJob job) {
        Instant completion = Instant.now();
        TriggerContext completed = job.getTriggerContext().withCompletion(completion);
        job.setTriggerContext(completed);

        if (job.getState() == JobState.CANCELLED || job.getState() == JobState.PAUSED) {
            return;
        }

        Optional<Instant> nextOpt = job.getTrigger().nextFireTime(completed);
        if (nextOpt.isEmpty() || !job.getTrigger().mayFireAgain()) {
            job.setState(JobState.COMPLETED);
            jobRepository.save(job);
            jobs.remove(job.getJobId());
            return;
        }

        Instant next = nextOpt.get();
        TriggerContext nextContext = completed.nextCycle(next);
        job.setTriggerContext(nextContext);
        job.setNextFireTime(next);
        job.setState(JobState.SCHEDULED);
        jobRepository.save(job);
        queue.offer(job);
        eventBus.publish(new TaskScheduledEvent(
                job.getJobId(),
                job.getTask().getId(),
                next,
                Instant.now()
        ));
        wakeOrchestrator();
    }

    private void waitForSignal(long timeoutMs) throws InterruptedException {
        synchronized (orchestratorMonitor) {
            orchestratorMonitor.wait(timeoutMs);
        }
    }

    private void wakeOrchestrator() {
        synchronized (orchestratorMonitor) {
            orchestratorMonitor.notifyAll();
        }
    }
}
