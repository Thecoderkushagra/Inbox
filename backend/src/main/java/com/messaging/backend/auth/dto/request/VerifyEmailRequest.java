package com.messaging.backend.auth.dto.request;

import com.messaging.backend.common.validation.annotation.NoHtml;
import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for email verification requests.
 *
 * <p>Purpose:
 * Transports the token provided by the user via the email link back to the server.
 */
public record VerifyEmailRequest(
    @NotBlank(message = "Verification token is required")
    @NoHtml
    String token
) {}
