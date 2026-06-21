package com.scheduler.triggers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public final class CronExpression {

    private final FieldMatcher minuteMatcher;
    private final FieldMatcher hourMatcher;
    private final FieldMatcher dayOfMonthMatcher;
    private final FieldMatcher monthMatcher;
    private final FieldMatcher dayOfWeekMatcher;
    private final ZoneId zoneId;

    public CronExpression(String expression) {
        this(expression, ZoneId.systemDefault());
    }

    public CronExpression(String expression, ZoneId zoneId) {
        this.zoneId = Objects.requireNonNull(zoneId);
        String[] parts = expression.trim().split("\\s+");
        if (parts.length != 5) {
            throw new IllegalArgumentException("Cron expression must have 5 fields: minute hour day month dayOfWeek");
        }
        minuteMatcher = FieldMatcher.parse(parts[0], 0, 59);
        hourMatcher = FieldMatcher.parse(parts[1], 0, 23);
        dayOfMonthMatcher = FieldMatcher.parse(parts[2], 1, 31);
        monthMatcher = FieldMatcher.parse(parts[3], 1, 12);
        dayOfWeekMatcher = FieldMatcher.parseDayOfWeek(parts[4]);
    }

    public String getExpression() {
        return String.join(" ",
                minuteMatcher.raw(),
                hourMatcher.raw(),
                dayOfMonthMatcher.raw(),
                monthMatcher.raw(),
                dayOfWeekMatcher.raw());
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public Instant nextAfter(Instant after) {
        ZonedDateTime cursor = LocalDateTime.ofInstant(after, zoneId)
                .withSecond(0)
                .withNano(0)
                .plusMinutes(1)
                .atZone(zoneId);

        for (int i = 0; i < 366 * 24 * 60; i++) {
            if (matches(cursor)) {
                return cursor.toInstant();
            }
            cursor = cursor.plusMinutes(1);
        }
        throw new IllegalStateException("Unable to compute next fire time for cron: " + getExpression());
    }

    private boolean matches(ZonedDateTime dateTime) {
        int dayOfWeek = dateTime.getDayOfWeek().getValue();
        return minuteMatcher.matches(dateTime.getMinute())
                && hourMatcher.matches(dateTime.getHour())
                && dayOfMonthMatcher.matches(dateTime.getDayOfMonth())
                && monthMatcher.matches(dateTime.getMonthValue())
                && dayOfWeekMatcher.matches(dayOfWeek);
    }

    private static final class FieldMatcher {

        private final String raw;
        private final Set<Integer> values;

        private FieldMatcher(String raw, Set<Integer> values) {
            this.raw = raw;
            this.values = values;
        }

        static FieldMatcher parse(String field, int min, int max) {
            return new FieldMatcher(field, expand(field, min, max));
        }

        static FieldMatcher parseDayOfWeek(String field) {
            if ("*".equals(field) || field.contains("/") || field.contains("-") || field.contains(",")) {
                return parse(field, 1, 7);
            }
            Set<Integer> values = new TreeSet<>();
            for (String token : field.split(",")) {
                values.add(parseDayOfWeekToken(token.trim()));
            }
            return new FieldMatcher(field, values);
        }

        private static int parseDayOfWeekToken(String token) {
            return switch (token.toUpperCase()) {
                case "SUN", "0", "7" -> 7;
                case "MON", "1" -> 1;
                case "TUE", "2" -> 2;
                case "WED", "3" -> 3;
                case "THU", "4" -> 4;
                case "FRI", "5" -> 5;
                case "SAT", "6" -> 6;
                default -> Integer.parseInt(token);
            };
        }

        private static Set<Integer> expand(String field, int min, int max) {
            if ("*".equals(field)) {
                return range(min, max);
            }
            if (field.startsWith("*/")) {
                int step = Integer.parseInt(field.substring(2));
                Set<Integer> values = new TreeSet<>();
                for (int i = min; i <= max; i += step) {
                    values.add(i);
                }
                return values;
            }
            Set<Integer> values = new TreeSet<>();
            for (String token : field.split(",")) {
                if (token.contains("-")) {
                    String[] bounds = token.split("-");
                    int start = Integer.parseInt(bounds[0]);
                    int end = Integer.parseInt(bounds[1]);
                    values.addAll(range(start, end));
                } else {
                    values.add(Integer.parseInt(token));
                }
            }
            return values;
        }

        private static Set<Integer> range(int start, int end) {
            Set<Integer> values = new TreeSet<>();
            for (int i = start; i <= end; i++) {
                values.add(i);
            }
            return values;
        }

        boolean matches(int value) {
            return values.contains(value);
        }

        String raw() {
            return raw;
        }
    }
}
