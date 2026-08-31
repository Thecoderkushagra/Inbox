package com.messaging.backend.auth.dto.response;

import com.messaging.backend.auth.enums.UserStatus;

import java.time.Instant;

/**
 * Data Transfer Object for successful user registration responses.
 */
public record RegisterResponse(
    String userId,
    String username,
    String email,
    UserStatus status,
    Instant createdAt,
    boolean verificationRequired
) {}
