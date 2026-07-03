package com.messaging.backend.messaging.repository;

import com.messaging.backend.messaging.entity.Message;
import com.messaging.backend.messaging.enums.MessageStatus;
import com.messaging.backend.messaging.enums.MessageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for managing Message entities.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    /**
     * Retrieves messages for a specific conversation in chronological order.
     *
     * @param conversationId the ID of the conversation
     * @param pageable       the pagination information
     * @return a page of messages
     */
    Page<Message> findByConversationIdOrderByCreatedAtAsc(UUID conversationId, Pageable pageable);

    /**
     * Retrieves messages for a specific conversation in reverse chronological order (newest first).
     *
     * @param conversationId the ID of the conversation
     * @param pageable       the pagination information
     * @return a page of messages
     */
    Page<Message> findByConversationIdOrderByCreatedAtDesc(UUID conversationId, Pageable pageable);

    /**
     * Retrieves messages sent by a specific user.
     *
     * @param senderId the ID of the sender
     * @param pageable the pagination information
     * @return a page of messages
     */
    Page<Message> findBySenderId(UUID senderId, Pageable pageable);

    /**
     * Retrieves messages for a conversation filtered by their delivery/read status.
     *
     * @param conversationId the ID of the conversation
     * @param status         the status to filter by
     * @param pageable       the pagination information
     * @return a page of messages
     */
    Page<Message> findByConversationIdAndStatus(UUID conversationId, MessageStatus status, Pageable pageable);

    /**
     * Retrieves messages for a conversation filtered by their structural type (e.g., IMAGE, FILE).
     *
     * @param conversationId the ID of the conversation
     * @param messageType    the message type to filter by
     * @param pageable       the pagination information
     * @return a page of messages
     */
    Page<Message> findByConversationIdAndMessageType(UUID conversationId, MessageType messageType, Pageable pageable);

    /**
     * Retrieves active (non-deleted) messages for a conversation in chronological order.
     *
     * @param conversationId the ID of the conversation
     * @param pageable       the pagination information
     * @return a page of non-deleted messages
     */
    Page<Message> findByConversationIdAndDeletedFalseOrderByCreatedAtAsc(UUID conversationId, Pageable pageable);

    /**
     * Retrieves deleted messages for a conversation.
     *
     * @param conversationId the ID of the conversation
     * @param pageable       the pagination information
     * @return a page of deleted messages
     */
    Page<Message> findByConversationIdAndDeletedTrue(UUID conversationId, Pageable pageable);

    /**
     * Checks if a specific message exists within a specific conversation.
     *
     * @param conversationId the ID of the conversation
     * @param messageId      the ID of the message
     * @return true if the message belongs to the conversation, false otherwise
     */
    boolean existsByConversationIdAndId(UUID conversationId, UUID messageId);

    /**
     * Counts the total number of messages in a conversation.
     *
     * @param conversationId the ID of the conversation
     * @return the total number of messages
     */
    long countByConversationId(UUID conversationId);

    /**
     * Searches active (non-deleted) messages within a specific conversation by keyword,
     * ordered from newest to oldest.
     * Used for Message Search.
     *
     * @param conversationId the ID of the conversation
     * @param content        the keyword to search for
     * @param pageable       the pagination information
     * @return a page of matching messages
     */
    Page<Message> findByConversationIdAndDeletedFalseAndContentContainingIgnoreCaseOrderByCreatedAtDesc(
            UUID conversationId, String content, Pageable pageable);
}
