package com.messaging.backend.common.security.config;

import com.messaging.backend.common.config.CorsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

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
public class CorsConfiguration {

    private final CorsProperties corsProperties;

    public CorsConfiguration(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
