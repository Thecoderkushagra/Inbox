package com.messaging.backend.common.security.exception;

/**
 * Thrown when a JWT token fails cryptographic validation or logic checks.
 *
 * <p>Purpose:
 * Covers generic token invalidity scenarios (e.g. signature mismatch, wrong issuer).
 *
 * <p>Responsibilities:
 * Indicates that the token provided cannot be trusted.
 *
 * <p>Extension points:
 * None immediately necessary.
 */
public class JwtInvalidTokenException extends JwtAuthenticationException {
    public JwtInvalidTokenException(String msg, Throwable cause) {
        super(msg, cause);
    }
    public JwtInvalidTokenException(String msg) {
        super(msg);
    }
}
