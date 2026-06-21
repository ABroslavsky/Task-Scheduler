package com.scheduler.triggers;

import com.scheduler.core.TriggerContext;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;

public final class CronTrigger implements Trigger {

    private final CronExpression cronExpression;

    public CronTrigger(String expression) {
        this(new CronExpression(expression));
    }

    public CronTrigger(String expression, ZoneId zoneId) {
        this(new CronExpression(expression, zoneId));
    }

    public CronTrigger(CronExpression cronExpression) {
        this.cronExpression = Objects.requireNonNull(cronExpression);
    }

    public CronExpression getCronExpression() {
        return cronExpression;
    }

    @Override
    public Optional<Instant> nextFireTime(TriggerContext context) {
        Instant after = context.getPreviousFireTime()
                .or(() -> context.getActualFireTime())
                .orElse(Instant.now().minusSeconds(1));
        if (context.getPreviousFireTime().isEmpty() && context.getActualFireTime().isEmpty()) {
            Instant scheduled = context.getScheduledFireTime();
            Instant next = cronExpression.nextAfter(scheduled.minusSeconds(1));
            return Optional.of(next);
        }
        return Optional.of(cronExpression.nextAfter(after));
    }

    @Override
    public boolean mayFireAgain() {
        return true;
    }
}
