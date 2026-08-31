package com.messaging.backend.messaging.entity;

import com.messaging.backend.common.entity.BaseDocument;
import com.messaging.backend.messaging.enums.MessageStatus;
import com.messaging.backend.messaging.enums.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Represents a chat message document stored in MongoDB.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "messages")
@CompoundIndex(name = "idx_msg_conv_created", def = "{'conversationId': 1, 'createdAt': 1}")
@CompoundIndex(name = "idx_msg_sender_created", def = "{'senderId': 1, 'createdAt': -1}")
public class Message extends BaseDocument {

    @Indexed
    private String conversationId;

    @Indexed
    private String senderId;

    @NotBlank
    @Size(max = 5000)
    private String content;

    private MessageType messageType = MessageType.TEXT;

    private MessageStatus status = MessageStatus.SENT;

    private boolean edited = false;

    private Instant editedAt;

    private boolean deleted = false;

    private Instant deletedAt;

    @Builder
    public Message(String conversationId, String senderId, String content, MessageType messageType, MessageStatus status) {
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.content = content;
        this.messageType = messageType != null ? messageType : MessageType.TEXT;
        this.status = status != null ? status : MessageStatus.SENT;
        this.edited = false;
        this.deleted = false;
    }

    public void markEdited() {
        this.edited = true;
        this.editedAt = Instant.now();
    }

    public void markDeleted() {
        this.deleted = true;
        this.deletedAt = Instant.now();
        this.content = "[deleted]";
    }
}
