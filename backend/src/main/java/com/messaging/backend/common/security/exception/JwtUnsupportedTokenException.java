package com.messaging.backend.common.security.exception;

/**
 * Thrown when a JWT token uses unsupported features (e.g., unsupported algorithm).
 *
 * <p>Purpose:
 * Flags tokens that request cryptographic operations not supported by the application.
 *
 * <p>Responsibilities:
 * Prevents algorithm downgrade attacks.
 *
 * <p>Extension points:
 * None immediately necessary.
 */
public class JwtUnsupportedTokenException extends JwtAuthenticationException {
    public JwtUnsupportedTokenException(String msg, Throwable cause) {
        super(msg, cause);
    }
    public JwtUnsupportedTokenException(String msg) {
        super(msg);
    }
}
