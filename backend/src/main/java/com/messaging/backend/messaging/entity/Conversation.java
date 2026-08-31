package com.messaging.backend.messaging.entity;

import com.messaging.backend.common.entity.BaseDocument;
import com.messaging.backend.common.validation.annotation.NoHtml;
import com.messaging.backend.messaging.enums.ConversationType;
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
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a chat conversation in MongoDB with embedded participants.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "conversations")
@CompoundIndex(name = "idx_conv_type_last_msg", def = "{'type': 1, 'lastMessageAt': -1}")
@CompoundIndex(name = "idx_conv_participant_ids", def = "{'participantUserIds': 1}")
public class Conversation extends BaseDocument {

    @Indexed
    private ConversationType type;

    @Size(max = 100)
    @NoHtml
    private String title;

    @Size(max = 500)
    @NoHtml
    private String description;

    private Set<ConversationParticipant> participants = new HashSet<>();

    private Set<String> participantUserIds = new HashSet<>();

    @Indexed
    private Instant lastMessageAt;

    private boolean archived;

    @Builder
    public Conversation(ConversationType type, String title, String description, Instant lastMessageAt, boolean archived) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.lastMessageAt = lastMessageAt;
        this.archived = archived;
        this.participants = new HashSet<>();
        this.participantUserIds = new HashSet<>();
    }

    public void addParticipant(ConversationParticipant participant) {
        if (this.participants == null) {
            this.participants = new HashSet<>();
        }
        if (this.participantUserIds == null) {
            this.participantUserIds = new HashSet<>();
        }
        this.participants.add(participant);
        if (participant != null && participant.getUserId() != null) {
            this.participantUserIds.add(participant.getUserId());
        }
    }

    public void removeParticipant(String userId) {
        if (this.participants != null && userId != null) {
            this.participants.removeIf(p -> userId.equals(p.getUserId()));
        }
        if (this.participantUserIds != null && userId != null) {
            this.participantUserIds.remove(userId);
        }
    }
}
