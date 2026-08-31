package com.messaging.backend.websocket.exception;

import com.messaging.backend.common.exception.BadRequestException;
import com.messaging.backend.common.exception.ConflictException;
import com.messaging.backend.common.exception.ForbiddenException;
import com.messaging.backend.common.exception.ResourceNotFoundException;
import com.messaging.backend.common.security.exception.JwtAuthenticationException;
import com.messaging.backend.websocket.constant.WebSocketDestinations;
import com.messaging.backend.websocket.dto.response.WebSocketErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * Global exception handler for STOMP WebSocket controllers.
 * Intercepts exceptions thrown from @MessageMapping methods and routes
 * sanitized error responses strictly back to the originating user.
 */
@ControllerAdvice
public class WebSocketExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketExceptionHandler.class);

    @MessageExceptionHandler(BadRequestException.class)
    @SendToUser(WebSocketDestinations.USER_ERROR_QUEUE)
    public WebSocketErrorResponse handleBadRequestException(BadRequestException ex) {
        logger.warn("WebSocket BadRequestException: {}", ex.getMessage());
        return WebSocketErrorResponse.of("Bad Request", ex.getMessage());
    }

    @MessageExceptionHandler(ForbiddenException.class)
    @SendToUser(WebSocketDestinations.USER_ERROR_QUEUE)
    public WebSocketErrorResponse handleForbiddenException(ForbiddenException ex) {
        logger.warn("WebSocket ForbiddenException: {}", ex.getMessage());
        return WebSocketErrorResponse.of("Forbidden", ex.getMessage());
    }

    @MessageExceptionHandler(ResourceNotFoundException.class)
    @SendToUser(WebSocketDestinations.USER_ERROR_QUEUE)
    public WebSocketErrorResponse handleResourceNotFoundException(ResourceNotFoundException ex) {
        logger.warn("WebSocket ResourceNotFoundException: {}", ex.getMessage());
        return WebSocketErrorResponse.of("Not Found", ex.getMessage());
    }

    @MessageExceptionHandler(ConflictException.class)
    @SendToUser(WebSocketDestinations.USER_ERROR_QUEUE)
    public WebSocketErrorResponse handleConflictException(ConflictException ex) {
        logger.warn("WebSocket ConflictException: {}", ex.getMessage());
        return WebSocketErrorResponse.of("Conflict", ex.getMessage());
    }

    @MessageExceptionHandler(JwtAuthenticationException.class)
    @SendToUser(WebSocketDestinations.USER_ERROR_QUEUE)
    public WebSocketErrorResponse handleJwtAuthenticationException(JwtAuthenticationException ex) {
        logger.warn("WebSocket JwtAuthenticationException: {}", ex.getMessage());
        return WebSocketErrorResponse.of("Unauthorized", "Authentication failed");
    }

    @MessageExceptionHandler(Exception.class)
    @SendToUser(WebSocketDestinations.USER_ERROR_QUEUE)
    public WebSocketErrorResponse handleException(Exception ex) {
        logger.error("WebSocket Unexpected Exception: ", ex);
        return WebSocketErrorResponse.of("Internal Server Error", "An unexpected error occurred");
    }
}
