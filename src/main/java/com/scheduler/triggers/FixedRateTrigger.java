package com.scheduler.triggers;

import com.scheduler.core.TriggerContext;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public final class FixedRateTrigger implements Trigger {

    private final Duration period;
    private final Instant startTime;

    public FixedRateTrigger(Duration period) {
        this(period, Instant.now());
    }

    public FixedRateTrigger(Duration period, Instant startTime) {
        if (period.isNegative() || period.isZero()) {
            throw new IllegalArgumentException("Period must be positive");
        }
        this.period = Objects.requireNonNull(period);
        this.startTime = Objects.requireNonNull(startTime);
    }

    public Duration getPeriod() {
        return period;
    }

    public Instant getStartTime() {
        return startTime;
    }

    @Override
    public Optional<Instant> nextFireTime(TriggerContext context) {
        if (context.getActualFireTime().isEmpty() && context.getPreviousFireTime().isEmpty()) {
            Instant first = startTime.isAfter(context.getScheduledFireTime())
                    ? startTime
                    : context.getScheduledFireTime();
            return Optional.of(first);
        }
        Instant previousScheduled = context.getScheduledFireTime();
        return Optional.of(previousScheduled.plus(period));
    }

    @Override
    public boolean mayFireAgain() {
        return true;
    }
}
