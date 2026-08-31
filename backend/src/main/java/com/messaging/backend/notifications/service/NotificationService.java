package com.messaging.backend.notifications.service;

import com.messaging.backend.auth.entity.User;
import com.messaging.backend.auth.repository.UserRepository;
import com.messaging.backend.cache.constants.CacheConstants;
import com.messaging.backend.common.exception.ForbiddenException;
import com.messaging.backend.common.exception.ResourceNotFoundException;
import com.messaging.backend.notifications.dto.response.NotificationSocketResponse;
import com.messaging.backend.notifications.entity.Notification;
import com.messaging.backend.notifications.enums.NotificationType;
import com.messaging.backend.notifications.mapper.NotificationMapper;
import com.messaging.backend.notifications.repository.NotificationRepository;
import com.messaging.backend.pubsub.constants.PubSubChannels;
import com.messaging.backend.pubsub.dto.RedisEvent;
import com.messaging.backend.pubsub.publisher.RedisEventPublisher;
import com.messaging.backend.websocket.constant.WebSocketDestinations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationMapper notificationMapper;
    private final RedisEventPublisher redisEventPublisher;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository,
                               SimpMessagingTemplate messagingTemplate,
                               NotificationMapper notificationMapper,
                               RedisEventPublisher redisEventPublisher) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.notificationMapper = notificationMapper;
        this.redisEventPublisher = redisEventPublisher;
    }

    @Transactional
    @CacheEvict(value = CacheConstants.NOTIFICATION_CACHE, key = "'notification:unread-count:' + #recipientId")
    public Notification createNotification(String recipientId, NotificationType type, String title, String message, String referenceId) {
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found"));

        Notification notification = new Notification(recipient, type, title, message, referenceId);
        Notification saved = notificationRepository.save(notification);

        try {
            NotificationSocketResponse response = notificationMapper.toSocketResponse(saved);
            messagingTemplate.convertAndSendToUser(
                    recipientId,
                    WebSocketDestinations.NOTIFICATION_QUEUE,
                    response
            );

            redisEventPublisher.publish(PubSubChannels.NOTIFICATION_CHANNEL,
                    new RedisEvent(null, "NOTIFICATION", null, response, null));
        } catch (Exception e) {
            log.error("Failed to broadcast notification to user: {}", recipientId, e);
        }

        return saved;
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotifications(String recipientId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId);
    }

    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(String recipientId) {
        return notificationRepository.findByRecipientIdAndReadOrderByCreatedAtDesc(recipientId, false);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheConstants.NOTIFICATION_CACHE, key = "'notification:unread-count:' + #recipientId")
    public long getUnreadCount(String recipientId) {
        return notificationRepository.countByRecipientIdAndRead(recipientId, false);
    }

    @Transactional
    @CacheEvict(value = CacheConstants.NOTIFICATION_CACHE, key = "'notification:unread-count:' + #currentUserId")
    public Notification markAsRead(String notificationId, String currentUserId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        String recipientId = notification.getRecipientId() != null ? notification.getRecipientId() :
                (notification.getRecipient() != null ? notification.getRecipient().getId() : null);

        if (recipientId != null && !recipientId.equals(currentUserId)) {
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
    @CacheEvict(value = CacheConstants.NOTIFICATION_CACHE, key = "'notification:unread-count:' + #currentUserId")
    public void markAllAsRead(String currentUserId) {
        List<Notification> unreadNotifications = notificationRepository.findByRecipientIdAndReadOrderByCreatedAtDesc(currentUserId, false);

        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
            notification.setReadAt(Instant.now());
        }

        notificationRepository.saveAll(unreadNotifications);
    }
}
