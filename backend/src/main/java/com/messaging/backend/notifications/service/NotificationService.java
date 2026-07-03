package com.messaging.backend.notifications.service;

import com.messaging.backend.auth.entity.User;
import com.messaging.backend.auth.repository.UserRepository;
import com.messaging.backend.common.exception.ForbiddenException;
import com.messaging.backend.common.exception.ResourceNotFoundException;
import com.messaging.backend.notifications.entity.Notification;
import com.messaging.backend.notifications.enums.NotificationType;
import com.messaging.backend.notifications.repository.NotificationRepository;
import com.messaging.backend.notifications.dto.response.NotificationSocketResponse;
import com.messaging.backend.notifications.mapper.NotificationMapper;
import com.messaging.backend.websocket.constant.WebSocketDestinations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationMapper notificationMapper;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository,
                               SimpMessagingTemplate messagingTemplate, NotificationMapper notificationMapper) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.notificationMapper = notificationMapper;
    }

    @Transactional
    public Notification createNotification(UUID recipientId, NotificationType type, String title, String message, UUID referenceId) {
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found"));

        Notification notification = new Notification(recipient, type, title, message, referenceId);
        Notification saved = notificationRepository.save(notification);

        try {
            NotificationSocketResponse response = notificationMapper.toSocketResponse(saved);
            messagingTemplate.convertAndSendToUser(
                    recipientId.toString(),
                    WebSocketDestinations.NOTIFICATION_QUEUE,
                    response
            );
        } catch (Exception e) {
            log.error("Failed to broadcast notification to user: {}", recipientId, e);
        }

        return saved;
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotifications(UUID recipientId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId);
    }

    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(UUID recipientId) {
        return notificationRepository.findByRecipientIdAndReadOrderByCreatedAtDesc(recipientId, false);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UUID recipientId) {
        return notificationRepository.countByRecipientIdAndRead(recipientId, false);
    }

    @Transactional
    public Notification markAsRead(UUID notificationId, UUID currentUserId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getRecipient().getId().equals(currentUserId)) {
            throw new ForbiddenException("Cannot modify a notification that belongs to another user");
        }

        if (notification.isRead()) {
            return notification;
        }

        notification.setRead(true);
        notification.setReadAt(Instant.now());
        return notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(UUID currentUserId) {
        List<Notification> unreadNotifications = notificationRepository.findByRecipientIdAndReadOrderByCreatedAtDesc(currentUserId, false);

        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
            notification.setReadAt(Instant.now());
        }

        notificationRepository.saveAll(unreadNotifications);
    }
}
