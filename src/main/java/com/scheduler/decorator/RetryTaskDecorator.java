package com.scheduler.decorator;

import com.scheduler.events.EventBus;
import com.scheduler.retry.RetryPolicy;
import com.scheduler.tasks.Task;

import java.util.Objects;

public final class RetryTaskDecorator implements TaskDecorator {

    private final RetryPolicy retryPolicy;
    private final EventBus eventBus;
    private final String jobId;

    public RetryTaskDecorator(RetryPolicy retryPolicy, EventBus eventBus, String jobId) {
        this.retryPolicy = Objects.requireNonNull(retryPolicy);
        this.eventBus = Objects.requireNonNull(eventBus);
        this.jobId = Objects.requireNonNull(jobId);
    }

    @Override
    public Task decorate(Task task) {
        return new RetryingTask(task, retryPolicy, eventBus, jobId);
    }
}
