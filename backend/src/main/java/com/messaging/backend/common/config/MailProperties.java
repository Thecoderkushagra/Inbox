package com.messaging.backend.common.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Maps SMTP mail configuration properties.
 * Maps environment variables like SMTP_HOST, SMTP_PORT, SMTP_USERNAME, SMTP_FROM.
 * Exists to centrally manage all outbound email configurations for the platform.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "smtp")
public class MailProperties {

    /**
     * The SMTP server host.
     */
    @NotBlank(message = "SMTP host must be configured")
    private String host;

    /**
     * The SMTP server port.
     */
    @Min(value = 1, message = "Port must be valid")
    @Max(value = 65535, message = "Port must be valid")
    private int port;

    /**
     * The SMTP authentication username.
     */
    @NotBlank(message = "SMTP username must be configured")
    private String username;

    /**
     * The SMTP authentication password.
     */
    @NotBlank(message = "SMTP password must be configured")
    private String password;

    /**
     * The email address to send from.
     */
    @NotBlank(message = "SMTP from address must be configured")
    private String from;

}
