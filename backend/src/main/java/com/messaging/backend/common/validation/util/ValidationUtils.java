package com.messaging.backend.common.validation.util;

import java.util.regex.Pattern;

/**
 * Generic utility functions supporting validation logic.
 * 
 * Purpose:
 * Centralizes stateless String manipulation and evaluation for validation contexts.
 * 
 * Usage:
 * Call static methods directly from custom validators or services.
 * 
 * Limitations:
 * Intentionally kept thin. Heavy HTML parsing or business checks are forbidden here.
 * 
 * Extension points:
 * New generic validations (like URL safety) can be added as private/public static utilities.
 */
public final class ValidationUtils {

    // Simple pattern to detect angle brackets containing text
    private static final Pattern HTML_PATTERN = Pattern.compile("<[^>]+>");

    private ValidationUtils() {
        // Prevent instantiation
    }

    /**
     * Checks if a string contains non-whitespace text.
     * 
     * @param str the input string
     * @return true if string has text, false otherwise
     */
    public static boolean hasText(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * Identifies if a string contains likely HTML tags.
     * 
     * @param str the input string
     * @return true if HTML tags are detected, false otherwise
     */
    public static boolean containsHtml(String str) {
        if (str == null) {
            return false;
        }
        return HTML_PATTERN.matcher(str).find();
    }

    /**
     * Safely trims a string, handling nulls gracefully.
     * 
     * @param str the input string
     * @return trimmed string, or null if input was null
     */
    public static String safeTrim(String str) {
        return str == null ? null : str.trim();
    }
}
