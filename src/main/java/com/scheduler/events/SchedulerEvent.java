package com.scheduler.events;

import java.time.Instant;

public sealed interface SchedulerEvent permits
        TaskScheduledEvent,
        TaskStartedEvent,
        TaskCompletedEvent,
        TaskFailedEvent {

    String jobId();

    String taskId();

    Instant timestamp();
}
