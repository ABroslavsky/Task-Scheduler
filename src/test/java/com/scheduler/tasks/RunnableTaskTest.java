package com.scheduler.tasks;

import com.scheduler.core.JobBuilder;
import com.scheduler.core.Scheduler;
import com.scheduler.core.SchedulerConfig;
import com.scheduler.factory.SchedulerFactory;
import com.scheduler.factory.TriggerFactory;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RunnableTaskTest {

    @Test
    void executesWrappedRunnable() throws InterruptedException {
        Scheduler scheduler = SchedulerFactory.create(SchedulerConfig.builder()
                .orchestratorTickMs(20)
                .build());
        AtomicInteger runs = new AtomicInteger();

        scheduler.schedule(JobBuilder.create()
                .task(new RunnableTask("wrapped", runs::incrementAndGet))
                .withTrigger(TriggerFactory.fixedDelay(Duration.ofMillis(50)))
                .build());

        scheduler.start();
        Thread.sleep(200);
        scheduler.shutdown();

        assertTrue(runs.get() >= 2);
    }
}
