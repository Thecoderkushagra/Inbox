package com.messaging.backend.notifications.dto.response;

import java.time.Instant;

public record NotificationResponse(
        String notificationId,
        String type,
        String title,
        String message,
        String referenceId,
        boolean read,
        Instant readAt,
        Instant createdAt
) {
}
