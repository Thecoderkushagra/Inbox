package com.messaging.backend.common.util;

/**
 * Shared generic constants specifically for tests.
 * 
 * Purpose:
 * Centralizes magic numbers used in testing configurations or assertions to ensure
 * uniform defaults across all suites.
 * 
 * Usage:
 * Statically import these constants into test classes.
 * 
 * Extension points:
 * Expandable to include default generic mock responses or standard byte lengths.
 */
public final class TestConstants {

    private TestConstants() {
        // Prevent instantiation
    }

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final long TEST_TIMEOUT_MS = 5000L;
    public static final String DEFAULT_TIMEZONE = "UTC";
}
