package com.scheduler.retry;

import java.time.Duration;
import java.util.Objects;

public final class MaxAttemptsRetryPolicy implements RetryPolicy {

    private final int maxAttempts;
    private final Duration delay;

    public MaxAttemptsRetryPolicy(int maxAttempts, Duration delay) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        this.maxAttempts = maxAttempts;
        this.delay = Objects.requireNonNull(delay);
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public Duration getDelay() {
        return delay;
    }

    @Override
    public boolean shouldRetry(int attempt, Throwable error) {
        return attempt < maxAttempts;
    }

    @Override
    public Duration delayBeforeRetry(int attempt) {
        return delay;
    }
}
