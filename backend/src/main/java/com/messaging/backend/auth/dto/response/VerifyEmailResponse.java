package com.messaging.backend.auth.dto.response;

import com.messaging.backend.auth.enums.UserStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Data Transfer Object for successful email verification responses.
 *
 * <p>Purpose:
 * Provides the client with confirmation that the account has been activated.
 */
public record VerifyEmailResponse(
    UUID userId,
    String username,
    String email,
    UserStatus status,
    Instant verifiedAt
) {}
