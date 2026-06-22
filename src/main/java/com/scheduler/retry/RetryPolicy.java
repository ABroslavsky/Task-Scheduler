package com.scheduler.retry;

import java.time.Duration;

public interface RetryPolicy {

    boolean shouldRetry(int attempt, Throwable error);

    Duration delayBeforeRetry(int attempt);

    static RetryPolicy noRetry() {
        return NoRetryPolicy.INSTANCE;
    }

    static RetryPolicy maxAttempts(int maxAttempts, Duration delay) {
        return new MaxAttemptsRetryPolicy(maxAttempts, delay);
    }
}
