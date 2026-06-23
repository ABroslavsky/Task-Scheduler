package com.scheduler.triggers;

import com.scheduler.core.TriggerContext;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FixedDelayTriggerTest {

    @Test
    void firstFireUsesScheduledTime() {
        FixedDelayTrigger trigger = new FixedDelayTrigger(Duration.ofSeconds(5));
        Instant start = Instant.parse("2026-06-23T10:00:00Z");
        TriggerContext context = TriggerContext.initial(start);

        Instant next = trigger.nextFireTime(context).orElseThrow();

        assertEquals(start, next);
    }

    @Test
    void nextFireWaitsForCompletion() {
        FixedDelayTrigger trigger = new FixedDelayTrigger(Duration.ofSeconds(5));
        Instant start = Instant.parse("2026-06-23T10:00:00Z");
        Instant completion = Instant.parse("2026-06-23T10:00:03Z");
        TriggerContext context = TriggerContext.initial(start)
                .withFire(start)
                .withCompletion(completion);

        Instant next = trigger.nextFireTime(context).orElseThrow();

        assertEquals(Instant.parse("2026-06-23T10:00:08Z"), next);
    }

    @Test
    void mayFireAgain() {
        assertTrue(new FixedDelayTrigger(Duration.ofSeconds(1)).mayFireAgain());
    }
}
