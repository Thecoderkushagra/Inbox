package com.messaging.backend.common.security.util;

/**
 * Centralized registry of public API endpoints that do not require authentication.
 *
 * <p>Purpose:
 * Prevents magic strings in the SecurityConfiguration and provides a single source 
 * of truth for which endpoints are open to the public.
 *
 * <p>Lifecycle:
 * Constant values available for the lifetime of the application.
 *
 * <p>Extension points:
 * Additional endpoints like public webhooks or unauthenticated file downloads 
 * should be added here.
 */
public final class SecurityEndpoints {

    public static final String[] PUBLIC_ENDPOINTS = {
        "/api/v1/auth/**",
        "/actuator/health",
        "/swagger-ui/**",
        "/v3/api-docs/**"
    };

    private SecurityEndpoints() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}
