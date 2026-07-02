package com.messaging.backend.websocket.dto.response;

import java.time.Instant;

/**
 * DTO representing an error returned over a WebSocket connection.
 * Stripped of sensitive data such as stack traces or internal exception names.
 */
public record WebSocketErrorResponse(
        Instant timestamp,
        String error,
        String message
) {
    public static WebSocketErrorResponse of(String error, String message) {
        return new WebSocketErrorResponse(Instant.now(), error, message);
    }
}
