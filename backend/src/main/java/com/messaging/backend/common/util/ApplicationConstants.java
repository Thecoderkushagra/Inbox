package com.messaging.backend.common.util;

/**
 * Global application-wide constants.
 * 
 * Intended usage:
 * Use for non-business generic constants like HTTP header names, default pagination values,
 * or standard encoding names. Domain-specific constants belong in their respective modules.
 * 
 * Future extension points:
 * Expansion for standard generic headers or infrastructure variables not found in Spring constants.
 */
public final class ApplicationConstants {

    private ApplicationConstants() {
        // Prevent instantiation
    }

    public static final int DEFAULT_PAGE_NUMBER = 0;
    public static final int DEFAULT_PAGE_SIZE = 20;
    
    public static final String UTF_8 = "UTF-8";
}
