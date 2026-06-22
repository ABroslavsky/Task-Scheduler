package com.scheduler.factory;

import com.scheduler.core.Scheduler;
import com.scheduler.core.SchedulerConfig;
import com.scheduler.core.SchedulerEngine;
import com.scheduler.decorator.MetricsTaskDecorator;
import com.scheduler.events.EventBus;
import com.scheduler.execution.TaskExecutor;
import com.scheduler.execution.ThreadPoolTaskExecutor;
import com.scheduler.persistence.InMemoryJobRepository;
import com.scheduler.persistence.JobRepository;

import java.util.Objects;

public final class SchedulerFactory {

    private SchedulerFactory() {
    }

    public static Scheduler create() {
        return create(SchedulerConfig.defaults(), new InMemoryJobRepository());
    }

    public static Scheduler create(SchedulerConfig config) {
        return create(config, new InMemoryJobRepository());
    }

    public static Scheduler create(SchedulerConfig config, JobRepository jobRepository) {
        Objects.requireNonNull(config);
        Objects.requireNonNull(jobRepository);

        EventBus eventBus = new EventBus();
        MetricsTaskDecorator metricsTaskDecorator = new MetricsTaskDecorator();
        JobFactory jobFactory = new JobFactory(eventBus, metricsTaskDecorator);
        TaskExecutor taskExecutor = new ThreadPoolTaskExecutor(config, eventBus);
        SchedulerEngine engine = new SchedulerEngine(config, eventBus, taskExecutor, jobFactory, jobRepository);
        return new Scheduler(engine, eventBus, metricsTaskDecorator);
    }
}
