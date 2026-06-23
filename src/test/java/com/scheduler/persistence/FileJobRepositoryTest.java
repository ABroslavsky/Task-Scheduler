package com.scheduler.persistence;

import com.scheduler.demo.HeartbeatTask;
import com.scheduler.factory.JobFactory;
import com.scheduler.factory.TaskFactory;
import com.scheduler.factory.TriggerFactory;
import com.scheduler.core.ScheduledJob;
import com.scheduler.decorator.MetricsTaskDecorator;
import com.scheduler.events.EventBus;
import com.scheduler.retry.RetryPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileJobRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void roundTripPersistsJobDefinition() {
        TaskFactory taskFactory = new TaskFactory();
        taskFactory.register(HeartbeatTask.TASK_TYPE,
                () -> new HeartbeatTask("stored", tempDir.resolve("stored.log")));

        Path store = tempDir.resolve("jobs.dat");
        FileJobRepository repository = new FileJobRepository(store, taskFactory);

        HeartbeatTask task = new HeartbeatTask("stored", tempDir.resolve("stored.log"));
        JobFactory jobFactory = new JobFactory(new EventBus(), new MetricsTaskDecorator());
        ScheduledJob job = jobFactory.createScheduledJob(
                task,
                TriggerFactory.fixedDelay(Duration.ofSeconds(30)),
                RetryPolicy.noRetry(),
                Instant.parse("2026-06-23T12:00:00Z")
        );

        repository.save(job);

        FileJobRepository reloaded = new FileJobRepository(store, taskFactory);

        ScheduledJob loaded = reloaded.findById(job.getJobId()).orElseThrow();

        assertEquals(job.getJobId(), loaded.getJobId());
        assertEquals("stored", loaded.getTask().getMetadata().getName());
        assertTrue(loaded.getTrigger() instanceof com.scheduler.triggers.FixedDelayTrigger);
        assertEquals(1, reloaded.findAll().size());
    }
}
