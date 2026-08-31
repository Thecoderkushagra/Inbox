package com.messaging.backend.common.security.config;

import com.messaging.backend.common.config.CorsProperties;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configures Cross-Origin Resource Sharing (CORS) rules.
 *
 * <p>Purpose:
 * Specifies which external origins (e.g., frontend applications) are permitted 
 * to interact with this backend API, and what HTTP methods and headers are allowed.
 *
 * <p>Lifecycle:
 * Intercepts incoming requests before standard Spring Security filters to handle 
 * CORS preflight (OPTIONS) requests.
 *
 * <p>Extension points:
 * Can be enhanced to apply different CORS policies to different path patterns if needed.
 */
@Configuration
public class CorsConfig {

    private static final List<String> LOCALHOST_ORIGIN_PATTERNS = List.of(
            "http://localhost:[*]",
            "http://127.0.0.1:[*]",
            "http://localhost:5173",
            "http://localhost:3000",
            "http://localhost:4173",
            "http://localhost:8080",
            "http://127.0.0.1:5173",
            "http://127.0.0.1:3000",
            "http://127.0.0.1:8080",
            "http://127.0.0.1:4173"
    );

    private final CorsProperties corsProperties;

    public CorsConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        java.util.Set<String> patterns = new java.util.LinkedHashSet<>(LOCALHOST_ORIGIN_PATTERNS);
        if (corsProperties.getAllowedOrigins() != null) {
            patterns.addAll(corsProperties.getAllowedOrigins());
        }

        configuration.setAllowedOriginPatterns(new java.util.ArrayList<>(patterns));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
