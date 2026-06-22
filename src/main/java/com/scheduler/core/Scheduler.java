package com.scheduler.core;

import com.scheduler.decorator.MetricsTaskDecorator;
import com.scheduler.events.EventBus;
import com.scheduler.events.SchedulerEventListener;
import com.scheduler.retry.RetryPolicy;
import com.scheduler.tasks.Task;
import com.scheduler.triggers.Trigger;

import java.time.Instant;
import java.util.Optional;

public final class Scheduler {

    private final SchedulerEngine engine;
    private final EventBus eventBus;
    private final MetricsTaskDecorator metrics;

    public Scheduler(SchedulerEngine engine, EventBus eventBus, MetricsTaskDecorator metrics) {
        this.engine = engine;
        this.eventBus = eventBus;
        this.metrics = metrics;
    }

    public void start() {
        engine.start();
    }

    public void shutdown() {
        engine.shutdown();
    }

    public void shutdownNow() {
        engine.shutdownNow();
    }

    public String schedule(Task task, Trigger trigger) {
        return engine.schedule(task, trigger, RetryPolicy.noRetry(), Instant.now());
    }

    public String schedule(Task task, Trigger trigger, RetryPolicy retryPolicy) {
        return engine.schedule(task, trigger, retryPolicy, Instant.now());
    }

    public String schedule(JobBuilder.JobDefinition definition) {
        return engine.schedule(definition);
    }

    public boolean cancel(String jobId) {
        return engine.cancel(jobId);
    }

    public boolean pause(String jobId) {
        return engine.pause(jobId);
    }

    public boolean resume(String jobId) {
        return engine.resume(jobId);
    }

    public Optional<ScheduledJob> findJob(String jobId) {
        return engine.findJob(jobId);
    }

    public void addListener(SchedulerEventListener listener) {
        eventBus.register(listener);
    }

    public void removeListener(SchedulerEventListener listener) {
        eventBus.unregister(listener);
    }

    public MetricsTaskDecorator.TaskMetrics taskMetrics(String taskId) {
        return metrics.getMetrics(taskId);
    }
}
