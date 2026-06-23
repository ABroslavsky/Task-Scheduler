package com.scheduler.demo;

import com.scheduler.core.JobBuilder;
import com.scheduler.core.Scheduler;
import com.scheduler.core.SchedulerConfig;
import com.scheduler.events.SchedulerEvent;
import com.scheduler.events.SchedulerEventListener;
import com.scheduler.events.TaskFailedEvent;
import com.scheduler.factory.SchedulerFactory;
import com.scheduler.factory.TaskFactory;
import com.scheduler.factory.TriggerFactory;
import com.scheduler.persistence.FileJobRepository;
import com.scheduler.persistence.JobRepository;
import com.scheduler.retry.RetryPolicy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SchedulerDemo {

    private static final Logger LOGGER = Logger.getLogger(SchedulerDemo.class.getName());

    private SchedulerDemo() {
    }

    public static void main(String[] args) throws Exception {
        Path dataDir = Path.of("data");
        Files.createDirectories(dataDir);
        Path store = dataDir.resolve("jobs.dat");
        Path heartbeatLog = dataDir.resolve("heartbeat.log");
        Files.deleteIfExists(store);
        Files.deleteIfExists(heartbeatLog);

        TaskFactory taskFactory = new TaskFactory();
        taskFactory.register(HeartbeatTask.TASK_TYPE,
                () -> new HeartbeatTask("stored-heartbeat", dataDir.resolve("stored-heartbeat.log")));
        taskFactory.register(FlakyTask.TASK_TYPE, FlakyTask::new);

        JobRepository repository = new FileJobRepository(store, taskFactory);
        Scheduler scheduler = SchedulerFactory.create(SchedulerConfig.defaults(), repository);
        scheduler.addListener(new FailureListener());

        HeartbeatTask delayHeartbeat = new HeartbeatTask("delay-heartbeat", heartbeatLog);
        HeartbeatTask rateHeartbeat = new HeartbeatTask("rate-heartbeat", heartbeatLog);
        FlakyTask flakyTask = new FlakyTask();

        String rateJobId = scheduler.schedule(JobBuilder.create()
                .task(rateHeartbeat)
                .withTrigger(TriggerFactory.fixedRate(Duration.ofSeconds(1)))
                .build());

        scheduler.schedule(JobBuilder.create()
                .task(delayHeartbeat)
                .withTrigger(TriggerFactory.fixedDelay(Duration.ofSeconds(2)))
                .build());

        scheduler.schedule(JobBuilder.create()
                .task(flakyTask)
                .withTrigger(TriggerFactory.fixedDelay(Duration.ofSeconds(3)))
                .withRetry(RetryPolicy.maxAttempts(3, Duration.ofMillis(500)))
                .build());

        scheduler.schedule(JobBuilder.create()
                .task(new HeartbeatTask("cron-heartbeat", heartbeatLog))
                .withTrigger(TriggerFactory.cron("*/1 * * * *"))
                .build());

        scheduler.start();
        Thread.sleep(Duration.ofSeconds(3).toMillis());
        scheduler.pause(rateJobId);
        Thread.sleep(Duration.ofSeconds(2).toMillis());
        scheduler.resume(rateJobId);
        Thread.sleep(Duration.ofSeconds(7).toMillis());
        scheduler.shutdown();

        long logLines = Files.exists(heartbeatLog) ? Files.lines(heartbeatLog).count() : 0;
        LOGGER.log(Level.INFO, "delay-heartbeat runs={0}", delayHeartbeat.runs());
        LOGGER.log(Level.INFO, "rate-heartbeat runs={0}", rateHeartbeat.runs());
        LOGGER.log(Level.INFO, "heartbeat log lines={0}", logLines);
        LOGGER.log(Level.INFO, "persisted jobs={0}", repository.findAll().size());
    }

    private static final class FailureListener implements SchedulerEventListener {

        @Override
        public void onEvent(SchedulerEvent event) {
            if (event instanceof TaskFailedEvent failed) {
                LOGGER.log(Level.WARNING, "failed task={0} attempt={1}: {2}",
                        new Object[]{failed.taskId(), failed.attempt(), failed.error().getMessage()});
            }
        }
    }
}
