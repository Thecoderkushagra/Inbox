package com.messaging.backend.common.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

/**
 * Configures HTTP security headers.
 *
 * <p>Purpose:
 * Mitigates common web vulnerabilities like XSS, Clickjacking, and MIME sniffing 
 * by enforcing strict security headers in HTTP responses.
 *
 * <p>Lifecycle:
 * Applied once during the initialization of the SecurityFilterChain.
 *
 * <p>Extension points:
 * Content-Security-Policy can be updated with more precise directives as the frontend requirements mature.
 */
@Configuration
public class SecurityHeadersConfiguration {

    private final Environment environment;

    public SecurityHeadersConfiguration(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public Customizer<HeadersConfigurer<HttpSecurity>> securityHeadersCustomizer() {
        return headers -> headers
            .contentTypeOptions(Customizer.withDefaults())
            .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
            .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
            .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; frame-ancestors 'none'; object-src 'none'"))
            .permissionsPolicyHeader(permissions -> permissions.policy("geolocation=(), microphone=(), camera=()"))
            .httpStrictTransportSecurity(hsts -> {
                if (environment.acceptsProfiles(Profiles.of("prod"))) {
                    hsts.includeSubDomains(true).maxAgeInSeconds(31536000);
                } else {
                    hsts.disable();
                }
            });
    }
}
