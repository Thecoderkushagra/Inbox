package com.messaging.backend.auth.dto.request;

import com.messaging.backend.common.validation.annotation.NoHtml;
import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for user login requests.
 *
 * <p>Purpose:
 * Transports the user's identifier (username or email) and password for authentication.
 */
public record LoginRequest(
    @NotBlank(message = "Identifier is required")
    @NoHtml
    String identifier,

    @NotBlank(message = "Password is required")
    String password
) {}
