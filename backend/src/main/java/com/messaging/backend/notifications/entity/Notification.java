package com.messaging.backend.notifications.entity;

import com.messaging.backend.auth.entity.User;
import com.messaging.backend.common.entity.BaseDocument;
import com.messaging.backend.notifications.enums.NotificationType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Represents a user notification document stored in MongoDB.
 */
@Getter
@Setter
@Document(collection = "notifications")
@CompoundIndex(name = "idx_notif_recipient_read", def = "{'recipientId': 1, 'read': 1}")
@CompoundIndex(name = "idx_notif_recipient_created", def = "{'recipientId': 1, 'createdAt': -1}")
public class Notification extends BaseDocument {

    @Indexed
    private String recipientId;

    private User recipient;

    private NotificationType type;

    private String title;

    private String message;

    private String referenceId;

    private boolean read = false;

    private Instant readAt;

    public Notification() {
    }

    public Notification(User recipient, NotificationType type, String title, String message, String referenceId) {
        this.recipient = recipient;
        this.recipientId = recipient != null ? recipient.getId() : null;
        this.type = type;
        this.title = title;
        this.message = message;
        this.referenceId = referenceId;
        this.read = false;
        this.readAt = null;
    }
}
