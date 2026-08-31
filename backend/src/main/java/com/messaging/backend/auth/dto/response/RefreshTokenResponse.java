package com.messaging.backend.auth.dto.response;

import java.time.Instant;

/**
 * Data Transfer Object for successful refresh token responses.
 *
 * <p>Purpose:
 * Returns the newly generated access and refresh tokens along with their expiration timestamps.
 * Does not expose sensitive user information.
 */
public record RefreshTokenResponse(
    String accessToken,
    String refreshToken,
    Instant accessTokenExpiresAt,
    Instant refreshTokenExpiresAt
) {}
