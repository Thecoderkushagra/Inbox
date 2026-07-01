package com.messaging.backend.auth.repository;

import com.messaging.backend.auth.entity.Role;
import com.messaging.backend.auth.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing Role entity persistence.
 *
 * <p>Responsibility:
 * Facilitates the lookup of application roles to assign to users during 
 * registration or administration.
 *
 * <p>Aggregate Root:
 * Role is an independent aggregate used reference-wise across the application 
 * to define RBAC authorities.
 *
 * <p>Intended Future Usage:
 * Future use cases may involve checking role existence during initialization scripts,
 * or expanding queries to find roles by particular permission scopes if the model grows.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByName(RoleType roleType);

    boolean existsByName(RoleType roleType);
}
