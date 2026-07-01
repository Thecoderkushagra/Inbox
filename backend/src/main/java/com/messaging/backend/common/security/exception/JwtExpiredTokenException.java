package com.messaging.backend.common.security.exception;

/**
 * Thrown when a JWT token has expired.
 *
 * <p>Purpose:
 * Differentiates token expiration from other failures.
 *
 * <p>Responsibilities:
 * Indicates that the client should request a new token using their refresh token.
 *
 * <p>Extension points:
 * Handlers could use this specific exception to trigger a special error code.
 */
public class JwtExpiredTokenException extends JwtAuthenticationException {
    public JwtExpiredTokenException(String msg, Throwable cause) {
        super(msg, cause);
    }
    public JwtExpiredTokenException(String msg) {
        super(msg);
    }
}
