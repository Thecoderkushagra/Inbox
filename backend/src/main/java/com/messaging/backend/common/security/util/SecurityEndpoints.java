package com.messaging.backend.common.security.util;

/**
 * Centralized registry of public API endpoints that do not require HTTP Basic/Bearer filter blocking.
 */
public final class SecurityEndpoints {

    public static final String[] PUBLIC_ENDPOINTS = {
        "/api/v1/auth/**",
        "/ws/**",
        "/actuator/health",
        "/swagger-ui/**",
        "/v3/api-docs/**"
    };

    private SecurityEndpoints() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}
