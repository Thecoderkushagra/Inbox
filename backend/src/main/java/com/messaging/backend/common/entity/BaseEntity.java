package com.messaging.backend.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Base mapped superclass for all entities requiring auditing and UUID identifiers.
 * 
 * <p>Purpose:
 * Provides a common identifier and tracks the creation and modification timestamps 
 * and users for auditing purposes. Prevents duplication of auditing fields across entities.
 * 
 * <p>Lifecycle:
 * The identifier is generated automatically by JPA upon entity creation. Auditing fields 
 * are managed automatically by Spring Data JPA's {@link AuditingEntityListener}.
 * 
 * <p>Future extension points:
 * Additional common fields such as soft-delete flags (e.g., deletedAt, isDeleted) 
 * or versioning for optimistic locking (@Version) can be added here if they become 
 * globally required across the domain model.
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;

    /**
     * Protected no-args constructor required by JPA.
     */
    protected BaseEntity() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseEntity that)) return false;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
