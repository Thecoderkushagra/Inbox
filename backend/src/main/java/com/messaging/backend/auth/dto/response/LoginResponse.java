package com.messaging.backend.auth.dto.response;

import java.time.Instant;
import java.util.List;

/**
 * Data Transfer Object for successful login responses.
 */
public record LoginResponse(
    String userId,
    String username,
    String email,
    List<String> roles,
    String accessToken,
    String refreshToken,
    Instant accessTokenExpiresAt,
    Instant refreshTokenExpiresAt
) {}
