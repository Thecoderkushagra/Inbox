package com.messaging.backend.common.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Base exception for JWT related authentication errors.
 *
 * <p>Purpose:
 * Provides a common hierarchy for token-based authentication failures.
 *
 * <p>Responsibilities:
 * Acts as the parent for specific token exception types like expired or malformed tokens.
 *
 * <p>Extension points:
 * Can be caught generically in the AuthenticationEntryPoint.
 */
public class JwtAuthenticationException extends AuthenticationException {
    public JwtAuthenticationException(String msg, Throwable cause) {
        super(msg, cause);
    }
    public JwtAuthenticationException(String msg) {
        super(msg);
    }
}
