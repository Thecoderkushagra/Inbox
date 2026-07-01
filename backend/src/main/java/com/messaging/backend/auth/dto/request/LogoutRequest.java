package com.messaging.backend.auth.dto.request;

import com.messaging.backend.common.validation.annotation.NoHtml;
import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for user logout requests.
 *
 * <p>Purpose:
 * Transports the refresh token that needs to be revoked to end the session.
 */
public record LogoutRequest(
    @NotBlank(message = "Refresh token is required")
    @NoHtml
    String refreshToken
) {}
