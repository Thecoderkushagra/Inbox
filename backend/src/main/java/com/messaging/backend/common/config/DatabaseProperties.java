package com.messaging.backend.common.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Maps database configuration properties.
 * Maps environment variables like DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD.
 * Isolating these properties allows strongly-typed access before establishing data source connections.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "db")
public class DatabaseProperties {

    /**
     * The database host address.
     */
    @NotBlank(message = "Database host must be configured")
    private String host;

    /**
     * The database port.
     */
    @Min(value = 1, message = "Port must be valid")
    @Max(value = 65535, message = "Port must be valid")
    private int port;

    /**
     * The database name.
     */
    @NotBlank(message = "Database name must be configured")
    private String database;

    /**
     * The database username.
     */
    @NotBlank(message = "Database username must be configured")
    private String username;

    /**
     * The database password.
     */
    @NotBlank(message = "Database password must be configured")
    private String password;

}
