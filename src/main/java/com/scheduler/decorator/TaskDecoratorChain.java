package com.scheduler.decorator;

import com.scheduler.events.EventBus;
import com.scheduler.retry.RetryPolicy;
import com.scheduler.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class TaskDecoratorChain {

    private final List<TaskDecorator> decorators = new ArrayList<>();

    public TaskDecoratorChain withLogging() {
        decorators.add(new LoggingTaskDecorator());
        return this;
    }

    public TaskDecoratorChain withMetrics(MetricsTaskDecorator metricsDecorator) {
        decorators.add(metricsDecorator);
        return this;
    }

    public TaskDecoratorChain withRetry(RetryPolicy retryPolicy, EventBus eventBus, String jobId) {
        decorators.add(new RetryTaskDecorator(retryPolicy, eventBus, jobId));
        return this;
    }

    public Task apply(Task task) {
        Task decorated = Objects.requireNonNull(task);
        for (TaskDecorator decorator : decorators) {
            decorated = decorator.decorate(decorated);
        }
        return decorated;
    }
}
