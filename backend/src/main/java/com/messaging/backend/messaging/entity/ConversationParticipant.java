package com.messaging.backend.messaging.entity;

import com.messaging.backend.messaging.enums.ParticipantRole;
import com.messaging.backend.messaging.enums.ParticipantStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;

/**
 * Embedded participant subdocument stored within a Conversation MongoDB document.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
public class ConversationParticipant {

    private String userId;

    private ParticipantRole role;

    private ParticipantStatus status;

    private Instant joinedAt;

    private Instant lastReadAt;

    private boolean muted;

    private boolean pinned;

    public ConversationParticipant(String userId, ParticipantRole role, ParticipantStatus status, Instant joinedAt, Instant lastReadAt, boolean muted, boolean pinned) {
        this.userId = userId;
        this.role = role;
        this.status = status != null ? status : ParticipantStatus.ACTIVE;
        this.joinedAt = joinedAt != null ? joinedAt : Instant.now();
        this.lastReadAt = lastReadAt;
        this.muted = muted;
        this.pinned = pinned;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConversationParticipant that)) return false;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
