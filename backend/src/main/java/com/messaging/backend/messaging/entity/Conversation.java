package com.messaging.backend.messaging.entity;

import com.messaging.backend.common.entity.BaseEntity;
import com.messaging.backend.common.validation.annotation.NoHtml;
import com.messaging.backend.messaging.enums.ConversationType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "conversations",
    indexes = {
        @Index(name = "idx_conversation_type", columnList = "type"),
        @Index(name = "idx_conversation_last_message_at", columnList = "last_message_at")
    }
)
public class Conversation extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ConversationType type;

    @Size(max = 100)
    @NoHtml
    @Column(length = 100)
    private String title;

    @Size(max = 500)
    @NoHtml
    @Column(length = 500)
    private String description;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ConversationParticipant> participants = new HashSet<>();

    @OneToMany(mappedBy = "conversation", fetch = FetchType.LAZY)
    private List<Message> messages = new ArrayList<>();

    @Column(name = "last_message_at")
    private Instant lastMessageAt;

    @Column(nullable = false)
    private boolean archived;

    @Builder
    public Conversation(ConversationType type, String title, String description, Instant lastMessageAt, boolean archived) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.lastMessageAt = lastMessageAt;
        this.archived = archived;
    }
}
