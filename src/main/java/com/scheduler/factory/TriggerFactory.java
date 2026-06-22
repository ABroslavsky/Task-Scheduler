package com.scheduler.factory;

import com.scheduler.triggers.CronTrigger;
import com.scheduler.triggers.FixedDelayTrigger;
import com.scheduler.triggers.FixedRateTrigger;
import com.scheduler.triggers.Trigger;

import java.time.Duration;
import java.time.Instant;

public final class TriggerFactory {

    private TriggerFactory() {
    }

    public static Trigger fixedDelay(Duration delay) {
        return new FixedDelayTrigger(delay);
    }

    public static Trigger fixedRate(Duration period) {
        return new FixedRateTrigger(period);
    }

    public static Trigger fixedRate(Duration period, Instant startTime) {
        return new FixedRateTrigger(period, startTime);
    }

    public static Trigger cron(String expression) {
        return new CronTrigger(expression);
    }
}
