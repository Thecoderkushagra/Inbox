package com.messaging.backend.common.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.Instant;

/**
 * Generic base response wrapper for all API endpoints.
 * 
 * Purpose:
 * Enforces a strict, predictable JSON structure for every successful response.
 * 
 * Intended usage:
 * Should be returned by all REST controllers to wrap their specific domain payloads.
 * 
 * Future extension points:
 * Can be integrated with Spring AOP to automatically calculate processing time 
 * or inject request IDs into the metadata field.
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    private final boolean success;
    private final String message;
    private final T data;
    private final ResponseMetadata metadata;
    private final Instant timestamp;

    protected ApiResponse(boolean success, String message, T data, ResponseMetadata metadata, Instant timestamp) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.metadata = metadata;
        this.timestamp = timestamp;
    }
}
