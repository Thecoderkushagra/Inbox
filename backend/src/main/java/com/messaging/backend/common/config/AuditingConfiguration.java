package com.messaging.backend.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configures JPA Auditing for the application.
 * 
 * Intended usage:
 * Automatically populates @CreatedDate, @LastModifiedDate, @CreatedBy, and @LastModifiedBy fields
 * on JPA entities by utilizing the AuditorAware bean.
 * 
 * Future extension points:
 * Will automatically pick up identities once Spring Security is integrated via the auditorProvider.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditingConfiguration {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return new SpringSecurityAuditorAware();
    }
}
