package com.messaging.backend.common.exception;

import com.messaging.backend.common.exception.model.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Thrown when a requested resource cannot be located.
 * 
 * Intended usage:
 * Used across repositories or services when looking up a user, message, or group that does not exist.
 */
public class ResourceNotFoundException extends ApplicationException {

    public ResourceNotFoundException(String message) {
        super(message, ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}
