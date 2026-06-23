package com.scheduler.core;

import com.scheduler.demo.HeartbeatTask;
import com.scheduler.factory.SchedulerFactory;
import com.scheduler.factory.TriggerFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SchedulerRescheduleTest {

    @TempDir
    Path tempDir;

    @Test
    void fixedDelayJobRunsMultipleTimesAndReschedules() throws InterruptedException {
        SchedulerConfig config = SchedulerConfig.builder()
                .orchestratorTickMs(20)
                .build();
        Scheduler scheduler = SchedulerFactory.create(config);
        HeartbeatTask task = new HeartbeatTask("reschedule", tempDir.resolve("runs.log"));

        String jobId = scheduler.schedule(task, TriggerFactory.fixedDelay(Duration.ofMillis(80)));

        scheduler.start();
        Thread.sleep(300);
        scheduler.shutdown();

        assertTrue(task.runs() >= 2);
        Instant nextFire = scheduler.findJob(jobId)
                .map(ScheduledJob::getNextFireTime)
                .orElse(Instant.EPOCH);
        assertTrue(nextFire.isAfter(Instant.now().minusSeconds(1)));
    }

    @Test
    void cancelStopsFutureRuns() throws InterruptedException {
        SchedulerConfig config = SchedulerConfig.builder()
                .orchestratorTickMs(20)
                .build();
        Scheduler scheduler = SchedulerFactory.create(config);
        HeartbeatTask task = new HeartbeatTask("cancel", tempDir.resolve("cancel.log"));

        String jobId = scheduler.schedule(task, TriggerFactory.fixedRate(Duration.ofMillis(50)));

        scheduler.start();
        Thread.sleep(200);
        int runsBeforeCancel = task.runs();
        scheduler.cancel(jobId);
        Thread.sleep(200);
        scheduler.shutdown();

        assertTrue(runsBeforeCancel >= 1);
        assertTrue(task.runs() <= runsBeforeCancel + 1);
        assertTrue(scheduler.findJob(jobId).isEmpty());
    }
}
