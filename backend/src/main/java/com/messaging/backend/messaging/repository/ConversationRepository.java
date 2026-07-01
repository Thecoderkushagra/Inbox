package com.messaging.backend.messaging.repository;

import com.messaging.backend.messaging.entity.Conversation;
import com.messaging.backend.messaging.enums.ConversationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing Conversation entities.
 *
 * <p>Purpose:
 * Handles persistence and retrieval of chat rooms (conversations).
 * Uses Spring Data JPA derived queries to avoid native SQL and JPQL.
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    /**
     * Finds a conversation by its unique identifier.
     *
     * @param id the UUID of the conversation
     * @return an Optional containing the conversation if found, empty otherwise
     */
    Optional<Conversation> findById(UUID id);

    /**
     * Retrieves a paginated list of conversations by their type (DIRECT or GROUP).
     *
     * @param type the type of conversation to search for
     * @param pageable the pagination information
     * @return a Page of conversations matching the given type
     */
    Page<Conversation> findByType(ConversationType type, Pageable pageable);

    /**
     * Checks if a conversation exists with the given ID.
     *
     * @param id the UUID of the conversation
     * @return true if it exists, false otherwise
     */
    boolean existsById(UUID id);
}
