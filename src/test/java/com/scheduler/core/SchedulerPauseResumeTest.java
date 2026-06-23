package com.scheduler.core;

import com.scheduler.demo.HeartbeatTask;
import com.scheduler.factory.SchedulerFactory;
import com.scheduler.factory.TriggerFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SchedulerPauseResumeTest {

    @TempDir
    Path tempDir;

    @Test
    void pauseStopsSchedulingUntilResume() throws InterruptedException {
        SchedulerConfig config = SchedulerConfig.builder()
                .orchestratorTickMs(20)
                .build();
        Scheduler scheduler = SchedulerFactory.create(config);
        HeartbeatTask task = new HeartbeatTask("pause-test", tempDir.resolve("hb.log"));

        String jobId = scheduler.schedule(
                task,
                TriggerFactory.fixedRate(Duration.ofMillis(100))
        );

        scheduler.start();
        Thread.sleep(350);
        int countAtPause = task.runs();
        assertTrue(countAtPause >= 2);

        scheduler.pause(jobId);
        Thread.sleep(500);
        int whilePaused = task.runs();
        assertTrue(whilePaused <= countAtPause + 1);

        scheduler.resume(jobId);
        Thread.sleep(400);

        scheduler.shutdown();

        assertTrue(task.runs() > whilePaused);
    }
}
