package com.messaging.backend.common.exception;

import com.messaging.backend.common.exception.model.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * The base exception for all custom application errors.
 * 
 * Purpose:
 * Establishes a strict hierarchy enforcing that every expected failure carries
 * a predictable HTTP status and domain error code.
 * 
 * Intended usage:
 * Should not be thrown directly. Subclasses representing specific technical or 
 * business scenarios should extend this class.
 */
@Getter
public abstract class ApplicationException extends RuntimeException {

    private final ErrorCode errorCode;
    private final HttpStatus httpStatus;

    protected ApplicationException(String message, ErrorCode errorCode, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
    
    protected ApplicationException(String message, Throwable cause, ErrorCode errorCode, HttpStatus httpStatus) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
