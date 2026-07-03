package com.messaging.backend.notifications.dto.response;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID notificationId,
        String type,
        String title,
        String message,
        UUID referenceId,
        boolean read,
        Instant readAt,
        Instant createdAt
) {
}
