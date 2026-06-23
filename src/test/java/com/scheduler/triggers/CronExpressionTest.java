package com.scheduler.triggers;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CronExpressionTest {

    @Test
    void nextAfterEveryMinute() {
        CronExpression cron = new CronExpression("*/1 * * * *", ZoneOffset.UTC);
        Instant after = ZonedDateTime.of(2026, 6, 23, 10, 15, 30, 0, ZoneOffset.UTC).toInstant();

        Instant next = cron.nextAfter(after);

        assertEquals(
                ZonedDateTime.of(2026, 6, 23, 10, 16, 0, 0, ZoneOffset.UTC).toInstant(),
                next
        );
    }

    @Test
    void nextAfterStepSyntax() {
        CronExpression cron = new CronExpression("*/15 * * * *", ZoneOffset.UTC);
        Instant after = ZonedDateTime.of(2026, 6, 23, 10, 7, 0, 0, ZoneOffset.UTC).toInstant();

        Instant next = cron.nextAfter(after);

        assertEquals(
                ZonedDateTime.of(2026, 6, 23, 10, 15, 0, 0, ZoneOffset.UTC).toInstant(),
                next
        );
    }

    @Test
    void rejectsInvalidFieldCount() {
        assertThrows(IllegalArgumentException.class, () -> new CronExpression("0 * * *"));
    }
}
