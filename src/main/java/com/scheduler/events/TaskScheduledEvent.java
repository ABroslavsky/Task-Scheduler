package com.scheduler.events;

import java.time.Instant;

public record TaskScheduledEvent(
        String jobId,
        String taskId,
        Instant nextFireTime,
        Instant timestamp
) implements SchedulerEvent {
}
