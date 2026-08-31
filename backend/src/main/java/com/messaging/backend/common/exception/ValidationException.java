package com.messaging.backend.common.exception;

import com.messaging.backend.common.exception.model.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Thrown when business-level validation constraints fail.
 * 
 * Intended usage:
 * Differentiates business validation failures from framework-level Bean Validation.
 * Thrown manually by services when complex rules are broken.
 */
public class ValidationException extends ApplicationException {

    public ValidationException(String message) {
        super(message, ErrorCode.VALIDATION_ERROR, HttpStatus.valueOf(422));
    }
}
