package com.messaging.backend.common.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Maps general application configuration properties.
 * Maps environment variables like APP_NAME, APP_ENV, APP_VERSION.
 * This centralized configuration ensures strongly-typed access to application metadata.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app")
public class ApplicationProperties {

    /**
     * The name of the application.
     */
    @NotBlank(message = "Application name must be configured")
    private String name;

    /**
     * The environment the application is running in (e.g., development, production).
     */
    @NotBlank(message = "Application environment must be configured")
    private String environment;

    /**
     * The version of the application.
     */
    @NotBlank(message = "Application version must be configured")
    private String version;

}
