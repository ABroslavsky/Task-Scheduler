package com.scheduler.retry;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaxAttemptsRetryPolicyTest {

    @Test
    void allowsRetriesBelowMax() {
        MaxAttemptsRetryPolicy policy = new MaxAttemptsRetryPolicy(3, Duration.ofMillis(100));

        assertTrue(policy.shouldRetry(1, new RuntimeException()));
        assertTrue(policy.shouldRetry(2, new RuntimeException()));
        assertFalse(policy.shouldRetry(3, new RuntimeException()));
    }

    @Test
    void fixedDelayBetweenAttempts() {
        MaxAttemptsRetryPolicy policy = new MaxAttemptsRetryPolicy(2, Duration.ofMillis(250));

        assertEquals(Duration.ofMillis(250), policy.delayBeforeRetry(1));
    }
}
