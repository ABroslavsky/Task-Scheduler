package com.scheduler.events;

import java.time.Instant;

public record TaskCompletedEvent(
        String jobId,
        String taskId,
        long durationMs,
        Instant timestamp
) implements SchedulerEvent {
}
