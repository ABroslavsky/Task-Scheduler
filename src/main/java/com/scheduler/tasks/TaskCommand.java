package com.scheduler.tasks;

import com.scheduler.core.ScheduledJob;
import com.scheduler.events.EventBus;
import com.scheduler.events.TaskCompletedEvent;
import com.scheduler.events.TaskFailedEvent;
import com.scheduler.events.TaskStartedEvent;
import com.scheduler.execution.RejectionAwareCommand;

import java.time.Instant;
import java.util.Objects;
import java.util.function.Consumer;

public final class TaskCommand implements Runnable, RejectionAwareCommand {

    private final ScheduledJob job;
    private final Task decoratedTask;
    private final EventBus eventBus;
    private final Instant fireTime;
    private final Consumer<ScheduledJob> onComplete;
    private final int attempt;

    public TaskCommand(ScheduledJob job, Task decoratedTask, EventBus eventBus,
                       Instant fireTime, Consumer<ScheduledJob> onComplete) {
        this(job, decoratedTask, eventBus, fireTime, onComplete, 1);
    }

    public TaskCommand(ScheduledJob job, Task decoratedTask, EventBus eventBus,
                       Instant fireTime, Consumer<ScheduledJob> onComplete, int attempt) {
        this.job = Objects.requireNonNull(job);
        this.decoratedTask = Objects.requireNonNull(decoratedTask);
        this.eventBus = Objects.requireNonNull(eventBus);
        this.fireTime = Objects.requireNonNull(fireTime);
        this.onComplete = Objects.requireNonNull(onComplete);
        this.attempt = attempt;
    }

    @Override
    public void run() {
        Instant start = Instant.now();
        eventBus.publish(new TaskStartedEvent(job.getJobId(), job.getTask().getId(), fireTime, start));
        try {
            decoratedTask.execute();
            long durationMs = Instant.now().toEpochMilli() - start.toEpochMilli();
            eventBus.publish(new TaskCompletedEvent(job.getJobId(), job.getTask().getId(), durationMs, Instant.now()));
            onComplete.accept(job);
        } catch (Exception ex) {
            eventBus.publish(new TaskFailedEvent(job.getJobId(), job.getTask().getId(), ex, attempt, Instant.now()));
            job.setState(com.scheduler.core.JobState.SCHEDULED);
            onComplete.accept(job);
        }
    }

    @Override
    public String getJobId() {
        return job.getJobId();
    }

    @Override
    public String getTaskId() {
        return job.getTask().getId();
    }

    @Override
    public int getAttempt() {
        return attempt;
    }
}
