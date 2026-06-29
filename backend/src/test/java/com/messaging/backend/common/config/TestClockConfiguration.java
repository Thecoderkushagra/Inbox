package com.messaging.backend.common.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

/**
 * Configuration to override the application's production Clock with a fixed timestamp.
 * 
 * Purpose:
 * Enforces strict determinism across all tests interacting with dates and times.
 * 
 * Usage:
 * Automatically picked up by the Spring test context when explicitly imported or scanned.
 * 
 * Extension points:
 * The fixed instant could be extracted to a property or constant if dynamic injection is needed.
 */
@TestConfiguration
public class TestClockConfiguration {

    // A fixed point in time: 2026-01-01T00:00:00Z
    private static final Instant FIXED_INSTANT = Instant.parse("2026-01-01T00:00:00Z");

    @Bean
    @Primary
    public Clock fixedClock() {
        return Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);
    }
}
