package com.messaging.backend.common.security.exception;

/**
 * Thrown when a JWT token is structurally incorrect.
 *
 * <p>Purpose:
 * Identifies tokens that cannot even be parsed as JWTs.
 *
 * <p>Responsibilities:
 * Allows early rejection of poorly formed tokens.
 *
 * <p>Extension points:
 * None immediately necessary.
 */
public class JwtMalformedTokenException extends JwtAuthenticationException {
    public JwtMalformedTokenException(String msg, Throwable cause) {
        super(msg, cause);
    }
    public JwtMalformedTokenException(String msg) {
        super(msg);
    }
}
