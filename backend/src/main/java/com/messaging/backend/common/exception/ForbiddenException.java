package com.messaging.backend.common.exception;

import com.messaging.backend.common.exception.model.ErrorCode;
import org.springframework.http.HttpStatus;



/**
 * Exception thrown when a user is authenticated but not authorized to perform an action.
 *
 * <p>Purpose:
 * Signals a 403 Forbidden HTTP status, indicating the client does not have the necessary
 * permissions or the account state prohibits the action (e.g., suspended).
 */
public class ForbiddenException extends ApplicationException {

    public ForbiddenException(String message) {
        super(message, ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause, ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN);
    }
}
