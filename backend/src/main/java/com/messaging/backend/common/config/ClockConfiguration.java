package com.messaging.backend.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Provides a centralized Clock instance for the application.
 * 
 * Intended usage:
 * Inject this bean instead of calling Instant.now() or ZonedDateTime.now() directly.
 * 
 * Future extension points:
 * This ensures all timestamps are generated in UTC and allows the clock to be easily mocked in unit tests.
 */
@Configuration
public class ClockConfiguration {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
