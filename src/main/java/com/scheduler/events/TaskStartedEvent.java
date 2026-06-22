package com.scheduler.events;

import java.time.Instant;

public record TaskStartedEvent(
        String jobId,
        String taskId,
        Instant fireTime,
        Instant timestamp
) implements SchedulerEvent {
}
