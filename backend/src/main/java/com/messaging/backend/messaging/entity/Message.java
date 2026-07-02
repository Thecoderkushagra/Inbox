package com.messaging.backend.messaging.entity;

import com.messaging.backend.auth.entity.User;
import com.messaging.backend.common.entity.BaseEntity;
import com.messaging.backend.messaging.enums.MessageStatus;
import com.messaging.backend.messaging.enums.MessageType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "messages",
    indexes = {
        @Index(name = "idx_message_conversation_id", columnList = "conversation_id"),
        @Index(name = "idx_message_sender_id", columnList = "sender_id"),
        @Index(name = "idx_message_created_at", columnList = "created_at")
    }
)
public class Message extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @NotBlank
    @Size(max = 5000)
    @Column(nullable = false, length = 5000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 32)
    private MessageType messageType = MessageType.TEXT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MessageStatus status = MessageStatus.SENT;

    @Column(nullable = false)
    private boolean edited = false;

    @Column(name = "edited_at")
    private Instant editedAt;

    @Column(nullable = false)
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Builder
    public Message(Conversation conversation, User sender, String content) {
        this.conversation = conversation;
        this.sender = sender;
        this.content = content;
        this.messageType = MessageType.TEXT;
        this.status = MessageStatus.SENT;
        this.edited = false;
        this.deleted = false;
    }

    /**
     * Marks the message as edited and sets the edit timestamp.
     */
    public void markEdited() {
        this.edited = true;
        this.editedAt = Instant.now();
    }

    /**
     * Marks the message as deleted, scrubs the content, and sets the deletion timestamp.
     * Does not physically remove the database row.
     */
    public void markDeleted() {
        this.deleted = true;
        this.deletedAt = Instant.now();
        this.content = "[deleted]";
    }
}
