package com.messaging.backend.common.exception;

import com.messaging.backend.common.exception.model.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Thrown when an unexpected technical failure occurs within the application.
 * 
 * Intended usage:
 * Used to explicitly throw a 500 status when the system encounters a known 
 * unrecoverable state, preserving an organized error envelope.
 */
public class InternalServerException extends ApplicationException {

    public InternalServerException(String message) {
        super(message, ErrorCode.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public InternalServerException(String message, Throwable cause) {
        super(message, cause, ErrorCode.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
