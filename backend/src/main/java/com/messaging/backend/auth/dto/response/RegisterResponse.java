package com.messaging.backend.auth.dto.response;

import com.messaging.backend.auth.enums.UserStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Data Transfer Object for successful user registration responses.
 *
 * <p>Purpose:
 * Provides the client with necessary user details post-registration while 
 * strictly omitting sensitive data like passwords or tokens.
 *
 * <p>Extension points:
 * Can be extended to include onboarding flags or tracking parameters.
 */
public record RegisterResponse(
    UUID userId,
    String username,
    String email,
    UserStatus status,
    Instant createdAt,
    boolean verificationRequired
) {}
