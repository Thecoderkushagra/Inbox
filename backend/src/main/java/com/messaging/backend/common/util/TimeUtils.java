package com.messaging.backend.common.util;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Generic time manipulation utilities.
 * 
 * Intended usage:
 * Provides standardized timestamp formatting and basic time conversions.
 * Always favors UTC over local timezones.
 * 
 * Future extension points:
 * Extensible for parsing strict custom formats if required by specific frontend clients.
 */
public final class TimeUtils {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    private TimeUtils() {
        // Prevent instantiation
    }

    /**
     * Formats an Instant into an ISO-8601 string.
     * 
     * @param instant the instant to format
     * @return ISO formatted string or null if instant is null
     */
    public static String toIsoString(Instant instant) {
        if (instant == null) {
            return null;
        }
        return ISO_FORMATTER.format(instant.atOffset(ZoneOffset.UTC));
    }
}
