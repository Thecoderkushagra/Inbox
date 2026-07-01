package com.messaging.backend.auth.dto.request;

import com.messaging.backend.common.validation.annotation.NoHtml;
import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for refresh token requests.
 *
 * <p>Purpose:
 * Transports the refresh token provided by the client to obtain a new access/refresh token pair.
 */
public record RefreshTokenRequest(
    @NotBlank(message = "Refresh token is required")
    @NoHtml
    String refreshToken
) {}
