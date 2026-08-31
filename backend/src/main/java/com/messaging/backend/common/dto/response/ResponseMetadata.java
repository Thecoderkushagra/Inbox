package com.messaging.backend.common.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Generic metadata container for API responses.
 * 
 * Purpose:
 * Carries non-business related metadata payload along with the response.
 * 
 * Intended usage:
 * Included inside the ApiResponse to expose diagnostic information such as
 * request processing times, server time, and API versions.
 * 
 * Future extension points:
 * May be extended with specific tracing IDs (like correlation IDs) for distributed tracing.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResponseMetadata(
        String requestId,
        Long processingTimeMs,
        String apiVersion,
        Instant serverTime
) {
    /**
     * Creates a basic metadata instance with the current server time.
     * @return a new ResponseMetadata instance
     */
    public static ResponseMetadata now() {
        return new ResponseMetadata(null, null, "1.0", Instant.now());
    }
}
