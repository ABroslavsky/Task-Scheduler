package com.scheduler.retry;

import java.time.Duration;

public final class NoRetryPolicy implements RetryPolicy {

    static final NoRetryPolicy INSTANCE = new NoRetryPolicy();

    private NoRetryPolicy() {
    }

    @Override
    public boolean shouldRetry(int attempt, Throwable error) {
        return false;
    }

    @Override
    public Duration delayBeforeRetry(int attempt) {
        return Duration.ZERO;
    }
}
