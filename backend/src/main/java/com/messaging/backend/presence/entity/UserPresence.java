package com.messaging.backend.presence.entity;

import com.messaging.backend.auth.entity.User;
import com.messaging.backend.common.entity.BaseEntity;
import com.messaging.backend.presence.enums.PresenceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Represents the current connection state of a user.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "user_presences")
public class UserPresence extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PresenceStatus status = PresenceStatus.OFFLINE;

    @Column(name = "last_seen")
    private Instant lastSeen;

    public UserPresence(User user) {
        this.user = user;
        this.status = PresenceStatus.OFFLINE;
    }

    public void updateStatus(PresenceStatus status) {
        this.status = status;
        if (status == PresenceStatus.OFFLINE) {
            this.lastSeen = Instant.now();
        }
    }
}
