package com.scheduler.triggers;

import com.scheduler.core.TriggerContext;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FixedRateTriggerTest {

    @Test
    void firstFireUsesScheduledTime() {
        FixedRateTrigger trigger = new FixedRateTrigger(
                Duration.ofSeconds(10),
                Instant.parse("2026-06-23T10:00:00Z")
        );
        TriggerContext context = TriggerContext.initial(Instant.parse("2026-06-23T10:00:00Z"));

        Instant next = trigger.nextFireTime(context).orElseThrow();

        assertEquals(Instant.parse("2026-06-23T10:00:00Z"), next);
    }

    @Test
    void nextFireAdvancesByPeriodRegardlessOfCompletion() {
        FixedRateTrigger trigger = new FixedRateTrigger(Duration.ofSeconds(10));
        Instant scheduled = Instant.parse("2026-06-23T10:00:00Z");
        TriggerContext context = TriggerContext.initial(scheduled)
                .withFire(scheduled)
                .withCompletion(Instant.parse("2026-06-23T10:00:07Z"));

        Instant next = trigger.nextFireTime(context).orElseThrow();

        assertEquals(Instant.parse("2026-06-23T10:00:10Z"), next);
    }
}
