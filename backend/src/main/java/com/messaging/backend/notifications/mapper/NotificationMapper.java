package com.messaging.backend.notifications.mapper;

import com.messaging.backend.notifications.dto.response.NotificationResponse;
import com.messaging.backend.notifications.entity.Notification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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

    public com.messaging.backend.notifications.dto.response.NotificationSocketResponse toSocketResponse(Notification notification) {
        if (notification == null) {
            return null;
        }

        return new com.messaging.backend.notifications.dto.response.NotificationSocketResponse(
                notification.getId(),
                notification.getRecipient().getId(),
                notification.getType() != null ? notification.getType().name() : null,
                notification.getTitle(),
                notification.getMessage(),
                notification.getReferenceId(),
                notification.getCreatedAt()
        );
    }

    public List<NotificationResponse> toResponseList(List<Notification> notifications) {
        if (notifications == null) {
            return null;
        }

        List<NotificationResponse> responses = new ArrayList<>();
        for (Notification notification : notifications) {
            responses.add(toResponse(notification));
        }
        return responses;
    }
}
