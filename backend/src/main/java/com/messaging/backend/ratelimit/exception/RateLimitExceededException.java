package com.messaging.backend.ratelimit.exception;

import com.messaging.backend.common.exception.ApplicationException;
import com.messaging.backend.common.exception.model.ErrorCode;
import org.springframework.http.HttpStatus;

public class RateLimitExceededException extends ApplicationException {

    private final long retryAfterSeconds;

    public RateLimitExceededException(String message, long retryAfterSeconds) {
        super(message, ErrorCode.TOO_MANY_REQUESTS, HttpStatus.TOO_MANY_REQUESTS);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
