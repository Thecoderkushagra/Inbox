package com.messaging.backend.common.security.config;

import com.messaging.backend.auth.security.CustomUserDetailsService;
import com.messaging.backend.common.security.filter.JwtAuthenticationFilter;
import com.messaging.backend.common.security.handler.JwtAccessDeniedHandler;
import com.messaging.backend.common.security.handler.JwtAuthenticationEntryPoint;
import com.messaging.backend.common.security.util.SecurityEndpoints;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

/**
 * Primary security configuration for the application.
 *
 * <p>Purpose:
 * Configures the HTTP security filter chain, dictating authentication requirements,
 * session management, CSRF protection, and CORS settings.
 *
 * <p>Responsibilities:
 * Registers the authentication providers, JWT filters, and customized exception handlers.
 *
 * <p>Extension points:
 * Will be extended to include custom JWT filters, exception handlers 
 * (AccessDeniedHandler, AuthenticationEntryPoint), and method security.
 */
@Configuration
public class SecurityConfiguration {

    private final Customizer<HeadersConfigurer<HttpSecurity>> securityHeadersCustomizer;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    public SecurityConfiguration(
            Customizer<HeadersConfigurer<HttpSecurity>> securityHeadersCustomizer,
            PasswordEncoder passwordEncoder,
            CustomUserDetailsService userDetailsService,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            JwtAccessDeniedHandler jwtAccessDeniedHandler) {
        this.securityHeadersCustomizer = securityHeadersCustomizer;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(securityHeadersCustomizer)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/api/v1/users/me").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/v1/users", "/api/v1/users/*").permitAll()
                .requestMatchers(SecurityEndpoints.PUBLIC_ENDPOINTS).permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
