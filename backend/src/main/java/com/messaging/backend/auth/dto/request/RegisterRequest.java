package com.messaging.backend.auth.dto.request;

import com.messaging.backend.common.validation.annotation.NoHtml;
import com.messaging.backend.common.validation.annotation.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for user registration requests.
 *
 * <p>Purpose:
 * Encapsulates the required fields to register a new user, ensuring all inputs
 * are validated before hitting the service layer.
 *
 * <p>Extension points:
 * Could be extended if terms of service acceptance or captchas are required.
 */
public record RegisterRequest(
    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
    @NoHtml
    String username,

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email format is invalid")
    String email,

    @NotBlank(message = "Password cannot be blank")
    @StrongPassword
    String password,

    @NotBlank(message = "Confirm password cannot be blank")
    String confirmPassword
) {}
