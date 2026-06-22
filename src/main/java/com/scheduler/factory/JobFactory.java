package com.scheduler.factory;

import com.scheduler.core.ScheduledJob;
import com.scheduler.core.TriggerContext;
import com.scheduler.decorator.MetricsTaskDecorator;
import com.scheduler.decorator.TaskDecoratorChain;
import com.scheduler.events.EventBus;
import com.scheduler.tasks.Task;
import com.scheduler.triggers.Trigger;

import java.time.Instant;
import java.util.Objects;

public final class JobFactory {

    private final EventBus eventBus;
    private final MetricsTaskDecorator metricsTaskDecorator;

    public JobFactory(EventBus eventBus, MetricsTaskDecorator metricsTaskDecorator) {
        this.eventBus = Objects.requireNonNull(eventBus);
        this.metricsTaskDecorator = Objects.requireNonNull(metricsTaskDecorator);
    }

    public ScheduledJob createScheduledJob(Task task, Trigger trigger,
                                           com.scheduler.retry.RetryPolicy retryPolicy, Instant startTime) {
        TriggerContext context = TriggerContext.initial(startTime);
        Instant nextFireTime = trigger.nextFireTime(context).orElse(startTime);
        return ScheduledJob.create(task, trigger, retryPolicy, nextFireTime);
    }

    public Task createDecoratedTask(ScheduledJob job) {
        TaskDecoratorChain chain = new TaskDecoratorChain()
                .withRetry(job.getRetryPolicy(), eventBus, job.getJobId())
                .withLogging()
                .withMetrics(metricsTaskDecorator);
        return chain.apply(job.getTask());
    }
}
