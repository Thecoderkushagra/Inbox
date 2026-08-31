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
    private List<String> allowedOrigins = new java.util.ArrayList<>(List.of(
            "http://localhost:5173",
            "http://localhost:3000",
            "http://localhost:4173",
            "http://localhost:8080",
            "http://127.0.0.1:5173",
            "http://127.0.0.1:3000",
            "http://127.0.0.1:8080",
            "http://127.0.0.1:4173"
    ));

}
