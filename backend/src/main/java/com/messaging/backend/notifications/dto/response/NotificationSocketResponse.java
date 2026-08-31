package com.messaging.backend.notifications.dto.response;

import java.time.Instant;

public record NotificationSocketResponse(
        String notificationId,
        String recipientId,
        String type,
        String title,
        String message,
        String referenceId,
        Instant createdAt
) {
}
