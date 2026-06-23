package com.scheduler.triggers;

import com.scheduler.core.TriggerContext;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CronTriggerTest {

    @Test
    void firstFireFromScheduledTime() {
        CronTrigger trigger = new CronTrigger("*/5 * * * *", ZoneOffset.UTC);
        Instant start = ZonedDateTime.of(2026, 6, 23, 10, 2, 0, 0, ZoneOffset.UTC).toInstant();
        TriggerContext context = TriggerContext.initial(start);

        Instant next = trigger.nextFireTime(context).orElseThrow();

        assertEquals(ZonedDateTime.of(2026, 6, 23, 10, 5, 0, 0, ZoneOffset.UTC).toInstant(), next);
    }

    @Test
    void subsequentFireUsesCronExpression() {
        CronTrigger trigger = new CronTrigger("*/5 * * * *", ZoneOffset.UTC);
        Instant first = ZonedDateTime.of(2026, 6, 23, 10, 5, 0, 0, ZoneOffset.UTC).toInstant();
        TriggerContext context = TriggerContext.initial(first)
                .withFire(first)
                .withCompletion(first.plusSeconds(1));

        Instant next = trigger.nextFireTime(context).orElseThrow();

        assertEquals(ZonedDateTime.of(2026, 6, 23, 10, 10, 0, 0, ZoneOffset.UTC).toInstant(), next);
    }

    @Test
    void mayFireAgain() {
        assertTrue(new CronTrigger("0 0 * * *").mayFireAgain());
    }
}
