package com.messaging.backend.presence.repository;

import com.messaging.backend.presence.entity.UserPresence;
import com.messaging.backend.presence.enums.PresenceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserPresenceRepository extends JpaRepository<UserPresence, UUID> {

    Optional<UserPresence> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    Page<UserPresence> findByStatus(PresenceStatus status, Pageable pageable);

    long countByStatus(PresenceStatus status);

}
