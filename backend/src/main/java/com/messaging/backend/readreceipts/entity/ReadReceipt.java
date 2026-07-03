package com.messaging.backend.readreceipts.entity;

import com.messaging.backend.auth.entity.User;
import com.messaging.backend.common.entity.BaseEntity;
import com.messaging.backend.messaging.entity.Message;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
    name = "read_receipts",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"message_id", "user_id"})
    },
    indexes = {
        @Index(name = "idx_readreceipt_message_id", columnList = "message_id"),
        @Index(name = "idx_readreceipt_user_id", columnList = "user_id")
    }
)
public class ReadReceipt extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "seen_at")
    private Instant seenAt;

    @Builder
    public ReadReceipt(Message message, User user, Instant deliveredAt, Instant seenAt) {
        this.message = message;
        this.user = user;
        this.deliveredAt = deliveredAt;
        this.seenAt = seenAt;
    }
}
