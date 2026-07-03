package com.messaging.backend.notifications.dto.response;

import java.time.Instant;
import java.util.UUID;

public record NotificationSocketResponse(
        UUID notificationId,
        UUID recipientId,
        String type,
        String title,
        String message,
        UUID referenceId,
        Instant createdAt
) {
}
