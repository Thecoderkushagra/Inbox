package com.messaging.backend.common.factory;

import java.time.Instant;
import java.util.UUID;

/**
 * Reusable helper for generating generic, non-business test data.
 * 
 * Purpose:
 * Decreases boilerplate when assembling test variables, ensuring tests remain isolated from 
 * hardcoded or magically shared string references.
 * 
 * Usage:
 * Call the static methods whenever randomized primitives or timestamps are needed.
 * 
 * Extension points:
 * Ready to be extended with further generic types (e.g., random numerics or byte arrays).
 */
public final class TestDataFactory {

    private TestDataFactory() {
        // Prevent instantiation
    }

    public static UUID randomUuid() {
        return UUID.randomUUID();
    }

    public static String randomEmail() {
        return "test-" + randomUuid().toString().substring(0, 8) + "@example.com";
    }

    public static String randomString(String prefix) {
        return prefix + "-" + randomUuid().toString().substring(0, 8);
    }

    public static Instant randomInstant() {
        // Returns an instant somewhere around recent times
        return Instant.now().minusSeconds((long) (Math.random() * 100000));
    }
}
