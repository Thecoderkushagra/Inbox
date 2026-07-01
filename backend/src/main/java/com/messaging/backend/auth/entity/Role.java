package com.messaging.backend.auth.entity;

import com.messaging.backend.auth.enums.RoleType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Represents a role that can be assigned to a user.
 *
 * <p>Purpose:
 * Defines the authorities and permissions granted to users within the system.
 * Used for Role-Based Access Control (RBAC).
 *
 * <p>Lifecycle:
 * Roles are typically created during application startup or via database migrations
 * and remain static.
 *
 * <p>Future extension points:
 * Could be linked to a finer-grained Permission entity in a Many-To-Many relationship 
 * if permissions need to be more dynamic.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "roles",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "name")
    }
)
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private RoleType name;

    @Column
    private String description;

    @Builder
    public Role(RoleType name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role role)) return false;
        return id != null && id.equals(role.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
