package com.messaging.backend.common.config;

import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

/**
 * Retrieves the currently authenticated user's ID or username for JPA auditing.
 * 
 * Intended usage:
 * Supplies the identity of the user making modifications to entities.
 * 
 * Future extension points:
 * Currently returns Optional.empty() as authentication is not yet implemented.
 * Once Spring Security is integrated, this will extract the identity from the SecurityContextHolder.
 */
public class SpringSecurityAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        // Return empty until authentication is implemented in a future milestone.
        return Optional.empty();
    }
}
