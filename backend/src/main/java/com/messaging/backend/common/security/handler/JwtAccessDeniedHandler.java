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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/**
 * Custom handler for access denied (forbidden) errors.
 *
 * <p>Purpose:
 * Handles scenarios where an authenticated user lacks the required permissions (roles)
 * to access a specific resource. Returns a standardized JSON error response.
 *
 * <p>Lifecycle:
 * Singleton bean utilized by the Spring Security filter chain during access control checks.
 *
 * <p>Extension points:
 * Could be extended to trigger alerts if a single user repeatedly attempts to access
 * forbidden resources.
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public JwtAccessDeniedHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiError apiError = new ApiError(
                Instant.now(),
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                ErrorCode.FORBIDDEN,
                "Access is denied",
                request.getRequestURI(),
                null
        );

        objectMapper.writeValue(response.getWriter(), apiError);
    }
}
