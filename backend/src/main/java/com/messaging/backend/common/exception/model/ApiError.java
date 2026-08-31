package com.messaging.backend.common.exception.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Standardized immutable error response model.
 * 
 * Purpose:
 * Provides clients with a predictable and easily parsable error structure.
 * 
 * Future usage:
 * Will be returned universally by the GlobalExceptionHandler for all API failures.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        ErrorCode errorCode,
        String message,
        String path,
        String requestId
) {}
