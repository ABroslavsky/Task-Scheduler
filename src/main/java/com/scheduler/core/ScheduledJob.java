package com.scheduler.core;

import com.scheduler.retry.RetryPolicy;
import com.scheduler.tasks.Task;
import com.scheduler.triggers.Trigger;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public final class ScheduledJob implements Comparable<ScheduledJob> {

    private final String jobId;
    private final Task task;
    private final Trigger trigger;
    private final RetryPolicy retryPolicy;
    private final AtomicReference<Instant> nextFireTime;
    private final AtomicReference<JobState> state;
    private volatile TriggerContext triggerContext;

    public ScheduledJob(String jobId, Task task, Trigger trigger, RetryPolicy retryPolicy, Instant nextFireTime) {
        this.jobId = Objects.requireNonNull(jobId);
        this.task = Objects.requireNonNull(task);
        this.trigger = Objects.requireNonNull(trigger);
        this.retryPolicy = Objects.requireNonNull(retryPolicy);
        this.nextFireTime = new AtomicReference<>(Objects.requireNonNull(nextFireTime));
        this.state = new AtomicReference<>(JobState.SCHEDULED);
        this.triggerContext = TriggerContext.initial(nextFireTime);
    }

    public static ScheduledJob create(Task task, Trigger trigger, RetryPolicy retryPolicy, Instant nextFireTime) {
        return new ScheduledJob(UUID.randomUUID().toString(), task, trigger, retryPolicy, nextFireTime);
    }

    @Override
    public int compareTo(ScheduledJob other) {
        int timeCompare = nextFireTime.get().compareTo(other.nextFireTime.get());
        if (timeCompare != 0) {
            return timeCompare;
        }
        return jobId.compareTo(other.jobId);
    }

    public String getJobId() {
        return jobId;
    }

    public Task getTask() {
        return task;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    public Instant getNextFireTime() {
        return nextFireTime.get();
    }

    public void setNextFireTime(Instant nextFireTime) {
        this.nextFireTime.set(nextFireTime);
    }

    public JobState getState() {
        return state.get();
    }

    public void setState(JobState state) {
        this.state.set(state);
    }

    public TriggerContext getTriggerContext() {
        return triggerContext;
    }

    public void setTriggerContext(TriggerContext triggerContext) {
        this.triggerContext = triggerContext;
    }

    public boolean isActive() {
        JobState current = state.get();
        return current == JobState.SCHEDULED || current == JobState.RUNNING;
    }
}
