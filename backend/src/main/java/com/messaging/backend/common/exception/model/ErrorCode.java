package com.messaging.backend.common.exception.model;

/**
 * Standardized error codes for the platform.
 * 
 * Purpose:
 * Provides a finite, predictable set of error states for clients to parse.
 * 
 * Future usage:
 * Will be extended with specific business codes (e.g., USER_ALREADY_EXISTS) in later milestones.
 */
public enum ErrorCode {
    INTERNAL_ERROR,
    VALIDATION_ERROR,
    BAD_REQUEST,
    RESOURCE_NOT_FOUND,
    CONFLICT,
    UNAUTHORIZED,
    FORBIDDEN
}
