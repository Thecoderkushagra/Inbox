package com.messaging.backend.common.config;

import com.messaging.backend.auth.security.AuthenticatedUser;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Retrieves the currently authenticated user's ID for MongoDB document auditing.
 */
public class SpringSecurityAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser user) {
            return Optional.ofNullable(user.getId());
        }
        return Optional.empty();
    }
}
