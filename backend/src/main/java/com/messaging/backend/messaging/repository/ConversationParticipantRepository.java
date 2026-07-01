package com.messaging.backend.messaging.repository;

import com.messaging.backend.messaging.entity.ConversationParticipant;
import com.messaging.backend.messaging.enums.ParticipantRole;
import com.messaging.backend.messaging.enums.ParticipantStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing ConversationParticipant entities.
 *
 * <p>Purpose:
 * Acts as the backbone for authorization and membership queries.
 * Handles persistence and retrieval of user memberships in conversations.
 */
@Repository
public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, UUID> {

    /**
     * Finds a specific membership by conversation ID and user ID.
     *
     * @param conversationId the UUID of the conversation
     * @param userId the UUID of the user
     * @return an Optional containing the participant if found, empty otherwise
     */
    Optional<ConversationParticipant> findByConversationIdAndUserId(UUID conversationId, UUID userId);

    /**
     * Retrieves all members of a specific conversation, with pagination.
     *
     * @param conversationId the UUID of the conversation
     * @param pageable the pagination information
     * @return a Page of conversation participants
     */
    Page<ConversationParticipant> findByConversationId(UUID conversationId, Pageable pageable);

    /**
     * Retrieves all memberships for a specific user, with pagination.
     *
     * @param userId the UUID of the user
     * @param pageable the pagination information
     * @return a Page of conversation participants for the user
     */
    Page<ConversationParticipant> findByUserId(UUID userId, Pageable pageable);

    /**
     * Checks if a user is a member of a specific conversation.
     *
     * @param conversationId the UUID of the conversation
     * @param userId the UUID of the user
     * @return true if the membership exists, false otherwise
     */
    boolean existsByConversationIdAndUserId(UUID conversationId, UUID userId);

    /**
     * Retrieves conversation members filtered by their role, with pagination.
     *
     * @param conversationId the UUID of the conversation
     * @param role the participant role to filter by
     * @param pageable the pagination information
     * @return a Page of conversation participants matching the role
     */
    Page<ConversationParticipant> findByConversationIdAndRole(UUID conversationId, ParticipantRole role, Pageable pageable);

    /**
     * Retrieves conversation members filtered by their status, with pagination.
     *
     * @param conversationId the UUID of the conversation
     * @param status the participant status to filter by
     * @param pageable the pagination information
     * @return a Page of conversation participants matching the status
     */
    Page<ConversationParticipant> findByConversationIdAndStatus(UUID conversationId, ParticipantStatus status, Pageable pageable);
}
