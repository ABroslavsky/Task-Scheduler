package com.scheduler.triggers;

import com.scheduler.core.TriggerContext;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public final class FixedDelayTrigger implements Trigger {

    private final Duration delay;

    public FixedDelayTrigger(Duration delay) {
        if (delay.isNegative() || delay.isZero()) {
            throw new IllegalArgumentException("Delay must be positive");
        }
        this.delay = Objects.requireNonNull(delay);
    }

    public Duration getDelay() {
        return delay;
    }

    @Override
    public Optional<Instant> nextFireTime(TriggerContext context) {
        if (context.getCompletionTime().isEmpty() && context.getPreviousFireTime().isEmpty()) {
            return Optional.of(context.getScheduledFireTime());
        }
        Instant base = context.getCompletionTime()
                .or(() -> context.getActualFireTime())
                .orElse(context.getScheduledFireTime());
        return Optional.of(base.plus(delay));
    }

    @Override
    public boolean mayFireAgain() {
        return true;
    }
}
