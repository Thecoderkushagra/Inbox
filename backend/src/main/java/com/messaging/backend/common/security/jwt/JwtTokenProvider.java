package com.messaging.backend.common.security.jwt;

import com.messaging.backend.common.config.JwtProperties;
import com.messaging.backend.common.security.exception.JwtExpiredTokenException;
import com.messaging.backend.common.security.exception.JwtInvalidTokenException;
import com.messaging.backend.common.security.exception.JwtMalformedTokenException;
import com.messaging.backend.common.security.exception.JwtUnsupportedTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * Component responsible for JWT generation and validation.
 *
 * <p>Purpose:
 * Centralizes all cryptographic operations related to JSON Web Tokens.
 * Generates Access and Refresh tokens and safely parses incoming tokens.
 *
 * <p>Responsibilities:
 * Creates standard JWTs. Translates JJWT exceptions into application-specific security exceptions.
 *
 * <p>Extension points:
 * Can be extended to support token revocation lists or multiple signing keys
 * (key rotation) if required.
 */
@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final SecretKey key;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String subject, List<String> roles) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(jwtProperties.getAccessTokenExpiration());

        return Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim(JwtTokenClaims.CLAIM_ROLES, roles)
                .claim(JwtTokenClaims.CLAIM_TOKEN_TYPE, JwtTokenClaims.TOKEN_TYPE_ACCESS)
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(String subject) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(jwtProperties.getRefreshTokenExpiration());

        return Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim(JwtTokenClaims.CLAIM_TOKEN_TYPE, JwtTokenClaims.TOKEN_TYPE_REFRESH)
                .signWith(key)
                .compact();
    }

    public JwtTokenClaims parseAndValidateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            @SuppressWarnings("unchecked")
            List<String> roles = claims.get(JwtTokenClaims.CLAIM_ROLES, List.class);

            return JwtTokenClaims.builder()
                    .subject(claims.getSubject())
                    .issuedAt(claims.getIssuedAt() != null ? claims.getIssuedAt().toInstant() : null)
                    .expiresAt(claims.getExpiration() != null ? claims.getExpiration().toInstant() : null)
                    .roles(roles)
                    .tokenType(claims.get(JwtTokenClaims.CLAIM_TOKEN_TYPE, String.class))
                    .build();
        } catch (SignatureException ex) {
            throw new JwtInvalidTokenException("Invalid JWT signature", ex);
        } catch (MalformedJwtException ex) {
            throw new JwtMalformedTokenException("Invalid JWT token", ex);
        } catch (ExpiredJwtException ex) {
            throw new JwtExpiredTokenException("Expired JWT token", ex);
        } catch (UnsupportedJwtException ex) {
            throw new JwtUnsupportedTokenException("Unsupported JWT token", ex);
        } catch (IllegalArgumentException ex) {
            throw new JwtInvalidTokenException("JWT claims string is empty", ex);
        }
    }
}
