package com.messaging.backend.auth.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object for successful login responses.
 *
 * <p>Purpose:
 * Returns the generated JWTs and safe user details to the client.
 */
public record LoginResponse(
    UUID userId,
    String username,
    String email,
    List<String> roles,
    String accessToken,
    String refreshToken,
    Instant accessTokenExpiresAt,
    Instant refreshTokenExpiresAt
) {}
