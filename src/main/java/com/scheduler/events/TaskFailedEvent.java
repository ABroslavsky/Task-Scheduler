package com.scheduler.events;

import java.time.Instant;

public record TaskFailedEvent(
        String jobId,
        String taskId,
        Throwable error,
        int attempt,
        Instant timestamp
) implements SchedulerEvent {
}
