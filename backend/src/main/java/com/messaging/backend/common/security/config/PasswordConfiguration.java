package com.messaging.backend.common.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configures the password encoding strategy.
 *
 * <p>Purpose:
 * Provides the global {@link PasswordEncoder} bean used for hashing and verifying passwords securely.
 *
 * <p>Lifecycle:
 * Singleton bean used whenever passwords need to be checked (e.g., login) or hashed (e.g., registration).
 *
 * <p>Extension points:
 * Could be swapped with Argon2 or a delegating password encoder if migration to a stronger algorithm is needed.
 */
@Configuration
public class PasswordConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
