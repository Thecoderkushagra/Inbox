package com.messaging.backend.notifications.mapper;

import com.messaging.backend.notifications.dto.response.NotificationResponse;
import com.messaging.backend.notifications.dto.response.NotificationSocketResponse;
import com.messaging.backend.notifications.entity.Notification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        if (notification == null) {
            return null;
        }

        return new NotificationResponse(
                notification.getId(),
                notification.getType() != null ? notification.getType().name() : null,
                notification.getTitle(),
                notification.getMessage(),
                notification.getReferenceId(),
                notification.isRead(),
                notification.getReadAt(),
                notification.getCreatedAt()
        );
    }

    public NotificationSocketResponse toSocketResponse(Notification notification) {
        if (notification == null) {
            return null;
        }

        String recipientId = notification.getRecipientId() != null ? notification.getRecipientId() :
                (notification.getRecipient() != null ? notification.getRecipient().getId() : null);

        return new NotificationSocketResponse(
                notification.getId(),
                recipientId,
                notification.getType() != null ? notification.getType().name() : null,
                notification.getTitle(),
                notification.getMessage(),
                notification.getReferenceId(),
                notification.getCreatedAt()
        );
    }

    public List<NotificationResponse> toResponseList(List<Notification> notifications) {
        if (notifications == null) {
            return Collections.emptyList();
        }

        List<NotificationResponse> responses = new ArrayList<>();
        for (Notification notification : notifications) {
            responses.add(toResponse(notification));
        }
        return responses;
    }
}
