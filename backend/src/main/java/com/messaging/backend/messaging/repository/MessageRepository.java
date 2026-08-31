package com.messaging.backend.messaging.repository;

import com.messaging.backend.messaging.entity.Message;
import com.messaging.backend.messaging.enums.MessageStatus;
import com.messaging.backend.messaging.enums.MessageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing Message MongoDB documents.
 */
@Repository
public interface MessageRepository extends MongoRepository<Message, String> {

    Page<Message> findByConversationIdOrderByCreatedAtAsc(String conversationId, Pageable pageable);

    Page<Message> findByConversationIdOrderByCreatedAtDesc(String conversationId, Pageable pageable);

    Page<Message> findBySenderId(String senderId, Pageable pageable);

    Page<Message> findByConversationIdAndStatus(String conversationId, MessageStatus status, Pageable pageable);

    Page<Message> findByConversationIdAndMessageType(String conversationId, MessageType messageType, Pageable pageable);

    Page<Message> findByConversationIdAndDeletedFalseOrderByCreatedAtAsc(String conversationId, Pageable pageable);

    Page<Message> findByConversationIdAndDeletedTrue(String conversationId, Pageable pageable);

    boolean existsByConversationIdAndId(String conversationId, String messageId);

    long countByConversationId(String conversationId);

    Page<Message> findByConversationIdAndDeletedFalseAndContentContainingIgnoreCaseOrderByCreatedAtDesc(
            String conversationId, String content, Pageable pageable);
}
