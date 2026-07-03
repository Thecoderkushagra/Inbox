package com.messaging.backend.messaging.repository;

import com.messaging.backend.messaging.entity.Conversation;
import com.messaging.backend.messaging.enums.ConversationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.messaging.backend.messaging.enums.ParticipantStatus;

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

    /**
     * Finds a conversation by its unique identifier and type.
     *
     * @param id the UUID of the conversation
     * @param type the type of conversation
     * @return an Optional containing the conversation if found, empty otherwise
     */
    Optional<Conversation> findByIdAndType(UUID id, ConversationType type);

    /**
     * Retrieves all conversations of a specific type where the user is a participant with a specific status.
     *
     * @param userId the UUID of the user
     * @param status the status of the participant
     * @param type the type of the conversation
     * @return a List of matching conversations
     */
    @Query("SELECT c FROM Conversation c JOIN c.participants p WHERE p.user.id = :userId AND p.status = :status AND c.type = :type")
    List<Conversation> findConversationsByParticipantUserAndStatusAndType(
            @Param("userId") UUID userId,
            @Param("status") ParticipantStatus status,
            @Param("type") ConversationType type);

    /**
     * Searches for conversations by title, filtered by type and user participation status.
     * Used for Group Search and specific Conversation Type searches.
     */
    @Query("SELECT c FROM Conversation c JOIN c.participants p WHERE p.user.id = :userId AND p.status = :status AND c.type = :type AND LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Conversation> searchConversationsByTypeAndTitle(
            @Param("userId") UUID userId,
            @Param("status") ParticipantStatus status,
            @Param("type") ConversationType type,
            @Param("query") String query,
            Pageable pageable);

    /**
     * Searches for conversations by title across all conversation types a user is participating in.
     * Used for Global Conversation Search.
     */
    @Query("SELECT c FROM Conversation c JOIN c.participants p WHERE p.user.id = :userId AND p.status = :status AND LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Conversation> searchConversationsByTitle(
            @Param("userId") UUID userId,
            @Param("status") ParticipantStatus status,
            @Param("query") String query,
            Pageable pageable);
}
