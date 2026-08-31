package com.messaging.backend.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Configures MongoDB Auditing for the application.
 */
@Configuration
@EnableMongoAuditing(auditorAwareRef = "auditorProvider")
public class AuditingConfiguration {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return new SpringSecurityAuditorAware();
    }
}
