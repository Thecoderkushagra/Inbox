package com.messaging.backend.common.util;

/**
 * Generic string manipulation utilities.
 * 
 * Intended usage:
 * Contains only framework-agnostic string operations to avoid duplicated logic.
 * 
 * Future extension points:
 * Add minimal strict checks here. For complex logic, favor standard libraries 
 * (like Apache Commons Lang) over adding to this class.
 */
public final class StringUtils {

    private StringUtils() {
        // Prevent instantiation
    }

    /**
     * Checks if a string is null or empty.
     * 
     * @param str the string to check
     * @return true if the string is null or empty, false otherwise
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * Checks if a string is null, empty, or contains only whitespace.
     * 
     * @param str the string to check
     * @return true if the string is blank, false otherwise
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
