package com.messaging.backend.common.exception;

import com.messaging.backend.common.exception.model.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Thrown when the client sends an invalid or malformed request.
 * 
 * Intended usage:
 * Catch-all for non-validation logical errors provided by the client,
 * such as missing required headers or logically flawed parameters.
 */
public class BadRequestException extends ApplicationException {

    public BadRequestException(String message) {
        super(message, ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST);
    }
}
