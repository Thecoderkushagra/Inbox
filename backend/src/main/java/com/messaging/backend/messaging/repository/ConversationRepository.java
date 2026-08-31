package com.messaging.backend.messaging.repository;

import com.messaging.backend.messaging.entity.Conversation;
import com.messaging.backend.messaging.enums.ConversationType;
import com.messaging.backend.messaging.enums.ParticipantStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing Conversation MongoDB documents.
 */
@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {

    Page<Conversation> findByType(ConversationType type, Pageable pageable);

    Optional<Conversation> findByIdAndType(String id, ConversationType type);

    @Query("{'participantUserIds': ?0}")
    Page<Conversation> findByParticipantUserId(String userId, Pageable pageable);

    @Query("{'participantUserIds': ?0}")
    List<Conversation> findAllByParticipantUserId(String userId);

    @Query("{'participants': {$elemMatch: {'userId': ?0, 'status': ?1}}, 'type': ?2}")
    List<Conversation> findConversationsByParticipantUserAndStatusAndType(
            String userId, ParticipantStatus status, ConversationType type);

    @Query("{'participants': {$elemMatch: {'userId': ?0, 'status': ?1}}, 'type': ?2, 'title': {$regex: ?3, $options: 'i'}}")
    Page<Conversation> searchConversationsByTypeAndTitle(
            String userId, ParticipantStatus status, ConversationType type, String query, Pageable pageable);

    @Query("{'participants': {$elemMatch: {'userId': ?0, 'status': ?1}}, 'title': {$regex: ?2, $options: 'i'}}")
    Page<Conversation> searchConversationsByTitle(
            String userId, ParticipantStatus status, String query, Pageable pageable);
}
