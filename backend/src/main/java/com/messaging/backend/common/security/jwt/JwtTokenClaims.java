package com.messaging.backend.common.security.jwt;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

/**
 * Immutable representation of application JWT claims.
 *
 * <p>Purpose:
 * Provides a strongly typed, framework-agnostic model for JWT payloads. Ensures
 * standard claims are structured predictably and cleanly separated from the underlying
 * JWT parsing library.
 *
 * <p>Lifecycle:
 * Created when parsing an incoming token or when preparing claims to generate a new token.
 * Short-lived, primarily used during the authentication request lifecycle.
 *
 * <p>Extension points:
 * Future claims such as 'deviceId' or 'sessionId' can be added here to support
 * concurrent session management or device tracking.
 */
@Getter
@Builder
public class JwtTokenClaims {

    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_TOKEN_TYPE = "tokenType";
    
    public static final String TOKEN_TYPE_ACCESS = "ACCESS";
    public static final String TOKEN_TYPE_REFRESH = "REFRESH";

    private final String subject;
    private final Instant issuedAt;
    private final Instant expiresAt;
    private final List<String> roles;
    private final String tokenType;
}
