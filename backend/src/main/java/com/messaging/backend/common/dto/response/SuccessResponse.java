package com.messaging.backend.common.dto.response;

import java.time.Instant;

/**
 * Specialized response wrapper for successful non-paginated responses.
 * 
 * Purpose:
 * Provides convenient static factory methods for creating standardized success payloads.
 * 
 * Intended usage:
 * Used directly in Controllers to wrap single objects or simple collections.
 * e.g., return ResponseEntity.ok(SuccessResponse.success(userDto));
 * 
 * Future extension points:
 * Can be overloaded to support setting explicit processing times or custom request IDs.
 */
public final class SuccessResponse<T> extends ApiResponse<T> {

    private SuccessResponse(String message, T data) {
        super(true, message, data, ResponseMetadata.now(), Instant.now());
    }

    /**
     * Creates a success response containing data.
     * 
     * @param data the payload
     * @param <T> the type of the payload
     * @return a structured success response
     */
    public static <T> SuccessResponse<T> success(T data) {
        return new SuccessResponse<>("Operation successful", data);
    }

    /**
     * Creates a success response containing a custom message and data.
     * 
     * @param message custom success message
     * @param data the payload
     * @param <T> the type of the payload
     * @return a structured success response
     */
    public static <T> SuccessResponse<T> success(String message, T data) {
        return new SuccessResponse<>(message, data);
    }
}
