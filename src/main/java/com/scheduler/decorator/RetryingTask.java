package com.scheduler.decorator;

import com.scheduler.events.EventBus;
import com.scheduler.events.TaskFailedEvent;
import com.scheduler.retry.RetryPolicy;
import com.scheduler.tasks.Task;

import java.time.Instant;
import java.util.Objects;

final class RetryingTask extends DelegatingTask {

    private final RetryPolicy retryPolicy;
    private final EventBus eventBus;
    private final String jobId;

    RetryingTask(Task delegate, RetryPolicy retryPolicy, EventBus eventBus, String jobId) {
        super(delegate);
        this.retryPolicy = Objects.requireNonNull(retryPolicy);
        this.eventBus = Objects.requireNonNull(eventBus);
        this.jobId = Objects.requireNonNull(jobId);
    }

    @Override
    public void execute() throws Exception {
        int attempt = 1;
        while (true) {
            try {
                delegate().execute();
                return;
            } catch (Exception ex) {
                if (!retryPolicy.shouldRetry(attempt, ex)) {
                    throw ex;
                }
                eventBus.publish(new TaskFailedEvent(
                        jobId,
                        getId(),
                        ex,
                        attempt,
                        Instant.now()
                ));
                Thread.sleep(retryPolicy.delayBeforeRetry(attempt).toMillis());
                attempt++;
            }
        }
    }
}
