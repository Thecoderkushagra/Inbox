package com.messaging.backend.common.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Maps JWT authentication configuration properties.
 * Maps environment variables like JWT_SECRET, JWT_ACCESS_EXPIRATION, JWT_REFRESH_EXPIRATION.
 * This class isolates security-related JWT configurations.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * The secret key used to sign JWTs.
     */
    @NotBlank(message = "JWT secret must be configured")
    private String secret;

    /**
     * Access token expiration time in milliseconds.
     */
    @Min(value = 60000, message = "Access token expiration must be at least 1 minute (60000 ms)")
    private long accessTokenExpiration;

    /**
     * Refresh token expiration time in milliseconds.
     */
    @Min(value = 86400000, message = "Refresh token expiration must be at least 1 day (86400000 ms)")
    private long refreshTokenExpiration;

}
