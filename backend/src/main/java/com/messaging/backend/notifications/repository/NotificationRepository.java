package com.messaging.backend.notifications.repository;

import com.messaging.backend.notifications.entity.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(String recipientId);

    List<Notification> findByRecipientIdAndReadOrderByCreatedAtDesc(String recipientId, boolean read);

    long countByRecipientIdAndRead(String recipientId, boolean read);

    boolean existsByIdAndRecipientId(String id, String recipientId);
}
