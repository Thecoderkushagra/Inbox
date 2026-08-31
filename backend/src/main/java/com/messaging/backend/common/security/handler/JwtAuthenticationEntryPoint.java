package com.messaging.backend.common.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.messaging.backend.common.exception.model.ApiError;
import com.messaging.backend.common.exception.model.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/**
 * Custom authentication entry point for unauthenticated requests.
 *
 * <p>Purpose:
 * Handles situations where an unauthenticated user attempts to access a protected resource.
 * Returns a standardized JSON error response instead of the default HTML or basic error.
 *
 * <p>Lifecycle:
 * Singleton bean utilized by the Spring Security filter chain during authorization failures.
 *
 * <p>Extension points:
 * Can be enhanced to log unauthorized access attempts for security monitoring.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiError apiError = new ApiError(
                Instant.now(),
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                ErrorCode.UNAUTHORIZED,
                "Full authentication is required to access this resource",
                request.getRequestURI(),
                null
        );

        objectMapper.writeValue(response.getWriter(), apiError);
    }
}
