package com.scheduler.core;

import com.scheduler.retry.RetryPolicy;
import com.scheduler.tasks.Task;
import com.scheduler.triggers.Trigger;

import java.time.Instant;
import java.util.Objects;

public final class JobBuilder {

    private Task task;
    private Trigger trigger;
    private RetryPolicy retryPolicy = RetryPolicy.noRetry();
    private Instant startTime = Instant.now();

    private JobBuilder() {
    }

    public static JobBuilder create() {
        return new JobBuilder();
    }

    public JobBuilder task(Task task) {
        this.task = Objects.requireNonNull(task);
        return this;
    }

    public JobBuilder withTrigger(Trigger trigger) {
        this.trigger = Objects.requireNonNull(trigger);
        return this;
    }

    public JobBuilder withRetry(RetryPolicy retryPolicy) {
        this.retryPolicy = Objects.requireNonNull(retryPolicy);
        return this;
    }

    public JobBuilder startAt(Instant startTime) {
        this.startTime = Objects.requireNonNull(startTime);
        return this;
    }

    public JobDefinition build() {
        if (task == null || trigger == null) {
            throw new IllegalStateException("Task and trigger are required");
        }
        return new JobDefinition(task, trigger, retryPolicy, startTime);
    }

    public record JobDefinition(Task task, Trigger trigger, RetryPolicy retryPolicy, Instant startTime) {
    }
}
