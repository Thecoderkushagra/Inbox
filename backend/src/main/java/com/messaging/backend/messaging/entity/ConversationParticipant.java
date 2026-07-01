package com.messaging.backend.messaging.entity;

import com.messaging.backend.auth.entity.User;
import com.messaging.backend.common.entity.BaseEntity;
import com.messaging.backend.messaging.enums.ParticipantRole;
import com.messaging.backend.messaging.enums.ParticipantStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
    name = "conversation_participants",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"conversation_id", "user_id"})
    },
    indexes = {
        @Index(name = "idx_conv_part_conversation_id", columnList = "conversation_id"),
        @Index(name = "idx_conv_part_user_id", columnList = "user_id"),
        @Index(name = "idx_conv_part_status", columnList = "status")
    }
)
public class ConversationParticipant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ParticipantRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ParticipantStatus status;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    @Column(name = "last_read_at")
    private Instant lastReadAt;

    @Column(nullable = false)
    private boolean muted;

    @Column(nullable = false)
    private boolean pinned;

    @Builder
    public ConversationParticipant(Conversation conversation, User user, ParticipantRole role, ParticipantStatus status, Instant joinedAt, Instant lastReadAt, boolean muted, boolean pinned) {
        this.conversation = conversation;
        this.user = user;
        this.role = role;
        this.status = status;
        this.joinedAt = joinedAt;
        this.lastReadAt = lastReadAt;
        this.muted = muted;
        this.pinned = pinned;
    }
}
