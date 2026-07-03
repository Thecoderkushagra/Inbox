package com.messaging.backend.common.exception.handler;

import com.messaging.backend.common.exception.ApplicationException;
import com.messaging.backend.common.exception.model.ApiError;
import com.messaging.backend.common.exception.model.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.messaging.backend.ratelimit.exception.RateLimitExceededException;

import java.time.Clock;
import java.time.Instant;

/**
 * Centralized global exception handler for all application errors.
 * <p>
 * Purpose:
 * Intercepts both expected business exceptions (ApplicationException) and unexpected technical
 * failures, converting them into standard ApiError payloads. Enforces the structured logging
 * strategy (WARN for expected, ERROR for unexpected, INFO for validation).
 * <p>
 * Future usage:
 * Will securely capture and mask any deep system errors ensuring stack traces are never exposed.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Clock clock;

    public GlobalExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiError> handleApplicationException(ApplicationException ex, HttpServletRequest request) {
        log.warn("Business Exception: [{}] {}", ex.getErrorCode(), ex.getMessage());
        return buildResponse(ex.getHttpStatus(), ex.getErrorCode(), ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.info("Validation Error: {}", ex.getMessage());
        return buildResponse(HttpStatus.valueOf(422), ErrorCode.VALIDATION_ERROR, "Invalid request payload", request.getRequestURI());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        log.info("Constraint Violation: {}", ex.getMessage());
        return buildResponse(HttpStatus.valueOf(422), ErrorCode.VALIDATION_ERROR, "Validation constraint violated", request.getRequestURI());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.info("Message Not Readable: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, "Malformed JSON request", request.getRequestURI());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParameter(MissingServletRequestParameterException ex, HttpServletRequest request) {
        log.info("Missing Parameter: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, "Missing required parameter", request.getRequestURI());
    }

    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(TypeMismatchException ex, HttpServletRequest request) {
        log.info("Type Mismatch: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, "Parameter type mismatch", request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected Internal Error: ", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR, "An unexpected technical error occurred", request.getRequestURI());
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiError> handleRateLimitExceeded(RateLimitExceededException ex, HttpServletRequest request) {
        log.warn("Rate Limit Exceeded: {}", ex.getMessage());
        ApiError apiError = new ApiError(
                Instant.now(clock),
                HttpStatus.TOO_MANY_REQUESTS.value(),
                HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
                ErrorCode.TOO_MANY_REQUESTS,
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(ex.getRetryAfterSeconds()))
                .body(apiError);
    }

    private ResponseEntity<ApiError> buildResponse(HttpStatus status, ErrorCode errorCode, String message, String path) {
        ApiError apiError = new ApiError(
                Instant.now(clock),
                status.value(),
                status.getReasonPhrase(),
                errorCode,
                message,
                path,
                null // requestId nullable for now
        );
        return new ResponseEntity<>(apiError, status);
    }
}
