package com.messaging.backend.common.config;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Maps CORS security configuration properties.
 * Maps environment variables like CORS_ALLOWED_ORIGINS.
 * Used to restrict which frontend origins can communicate with the backend.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {

    /**
     * List of allowed origins for Cross-Origin Resource Sharing.
     */
    @NotEmpty(message = "At least one CORS allowed origin must be configured")
    private List<String> allowedOrigins;

}
