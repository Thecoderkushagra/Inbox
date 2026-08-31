package com.messaging.backend.common.exception;

import com.messaging.backend.common.exception.model.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Thrown when an action conflicts with the current state of the system.
 * 
 * Intended usage:
 * Used when constraints are violated, such as duplicate unique keys (e.g., email already registered)
 * or invalid state transitions (e.g., accepting an already accepted friend request).
 */
public class ConflictException extends ApplicationException {

    public ConflictException(String message) {
        super(message, ErrorCode.CONFLICT, HttpStatus.CONFLICT);
    }
}
