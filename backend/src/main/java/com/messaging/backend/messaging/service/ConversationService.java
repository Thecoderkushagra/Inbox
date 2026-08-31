package com.messaging.backend.messaging.service;

import com.messaging.backend.auth.entity.User;
import com.messaging.backend.cache.constants.CacheConstants;
import com.messaging.backend.common.exception.BadRequestException;
import com.messaging.backend.common.exception.ConflictException;
import com.messaging.backend.common.exception.ForbiddenException;
import com.messaging.backend.common.exception.ResourceNotFoundException;
import com.messaging.backend.messaging.entity.Conversation;
import com.messaging.backend.messaging.entity.ConversationParticipant;
import com.messaging.backend.messaging.enums.ConversationType;
import com.messaging.backend.messaging.enums.ParticipantRole;
import com.messaging.backend.messaging.enums.ParticipantStatus;
import com.messaging.backend.messaging.repository.ConversationRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;

/**
 * Service for managing MongoDB conversations and embedded participants.
 */
@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;

    public ConversationService(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    /**
     * Creates a one-to-one private conversation between two users.
     */
    @Transactional
    public Conversation createPrivateConversation(User creator, User recipient) {
        if (creator == null || recipient == null) {
            throw new BadRequestException("Creator and recipient must not be null");
        }
        if (creator.getId().equals(recipient.getId())) {
            throw new BadRequestException("Creator and recipient cannot be the same user");
        }

        // Check if a direct conversation already exists between these users
        List<Conversation> existing = conversationRepository.findConversationsByParticipantUserAndStatusAndType(
                creator.getId(), ParticipantStatus.ACTIVE, ConversationType.DIRECT);
        for (Conversation c : existing) {
            if (c.getParticipantUserIds().contains(recipient.getId())) {
                return c;
            }
        }

        Conversation conversation = Conversation.builder()
                .type(ConversationType.DIRECT)
                .title(recipient.getUsername())
                .archived(false)
                .lastMessageAt(Instant.now())
                .build();

        ConversationParticipant creatorParticipant = ConversationParticipant.builder()
                .userId(creator.getId())
                .role(ParticipantRole.OWNER)
                .status(ParticipantStatus.ACTIVE)
                .joinedAt(Instant.now())
                .build();

        ConversationParticipant recipientParticipant = ConversationParticipant.builder()
                .userId(recipient.getId())
                .role(ParticipantRole.MEMBER)
                .status(ParticipantStatus.ACTIVE)
                .joinedAt(Instant.now())
                .build();

        conversation.addParticipant(creatorParticipant);
        conversation.addParticipant(recipientParticipant);

        return conversationRepository.save(conversation);
    }

    /**
     * Creates a new group conversation.
     */
    @Transactional
    public Conversation createGroupConversation(User owner, String name) {
        if (owner == null) {
            throw new BadRequestException("Owner must not be null");
        }
        if (!StringUtils.hasText(name)) {
            throw new BadRequestException("Group name must not be blank");
        }
        String trimmedName = name.trim();
        if (trimmedName.length() > 100) {
            throw new BadRequestException("Group name must not exceed 100 characters");
        }

        Conversation conversation = Conversation.builder()
                .type(ConversationType.GROUP)
                .title(trimmedName)
                .archived(false)
                .lastMessageAt(Instant.now())
                .build();

        ConversationParticipant ownerParticipant = ConversationParticipant.builder()
                .userId(owner.getId())
                .role(ParticipantRole.OWNER)
                .status(ParticipantStatus.ACTIVE)
                .joinedAt(Instant.now())
                .build();

        conversation.addParticipant(ownerParticipant);

        return conversationRepository.save(conversation);
    }

    /**
     * Retrieves a conversation by its String ID.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConstants.CONVERSATIONS_CACHE, key = "'conversation:' + #conversationId")
    public Conversation getConversation(String conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
    }

    /**
     * Retrieves a conversation and validates that the user is a member of it.
     */
    @Transactional(readOnly = true)
    public Conversation getConversationForUser(String conversationId, String userId) {
        Conversation conversation = getConversation(conversationId);
        if (!isParticipant(conversation, userId)) {
            throw new ForbiddenException("User is not a participant in this conversation");
        }
        return conversation;
    }

    /**
     * Retrieves all conversations for a user with pagination.
     */
    @Transactional(readOnly = true)
    public Page<Conversation> getUserConversations(String userId, Pageable pageable) {
        return conversationRepository.findByParticipantUserId(userId, pageable);
    }

    /**
     * Checks if a user is a participant in a conversation.
     */
    public boolean isParticipant(Conversation conversation, String userId) {
        if (conversation == null || userId == null) return false;
        return conversation.getParticipantUserIds().contains(userId) ||
                conversation.getParticipants().stream()
                        .anyMatch(p -> userId.equals(p.getUserId()) && p.getStatus() == ParticipantStatus.ACTIVE);
    }

    /**
     * Checks if a user is a participant in a conversation by conversation ID.
     */
    @Transactional(readOnly = true)
    public boolean isParticipant(String conversationId, String userId) {
        Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
        return isParticipant(conversation, userId);
    }

    /**
     * Adds a new participant to an existing conversation.
     */
    @Transactional
    @CacheEvict(value = CacheConstants.CONVERSATIONS_CACHE, key = "'conversation:' + #conversationId")
    public ConversationParticipant addParticipant(String requesterId, String conversationId, User newParticipant) {
        Conversation conversation = getConversation(conversationId);

        ConversationParticipant requester = findParticipant(conversation, requesterId);
        if (requester == null || requester.getStatus() != ParticipantStatus.ACTIVE) {
            throw new ForbiddenException("Requester is not an active participant");
        }

        if (requester.getRole() != ParticipantRole.OWNER && requester.getRole() != ParticipantRole.ADMIN) {
            throw new ForbiddenException("Only an OWNER or ADMIN can add participants");
        }

        if (isParticipant(conversation, newParticipant.getId())) {
            throw new ConflictException("User is already a participant in this conversation");
        }

        ConversationParticipant participant = ConversationParticipant.builder()
                .userId(newParticipant.getId())
                .role(ParticipantRole.MEMBER)
                .status(ParticipantStatus.ACTIVE)
                .joinedAt(Instant.now())
                .build();

        conversation.addParticipant(participant);
        conversationRepository.save(conversation);

        return participant;
    }

    /**
     * Removes a participant from an existing conversation.
     */
    @Transactional
    @CacheEvict(value = CacheConstants.CONVERSATIONS_CACHE, key = "'conversation:' + #conversationId")
    public void removeParticipant(String requesterId, String conversationId, String targetUserId) {
        Conversation conversation = getConversation(conversationId);

        if (requesterId.equals(targetUserId)) {
            throw new ForbiddenException("Owner cannot remove themselves. Use the leave endpoint instead.");
        }

        ConversationParticipant requester = findParticipant(conversation, requesterId);
        if (requester == null || requester.getRole() != ParticipantRole.OWNER) {
            throw new ForbiddenException("Only the OWNER can remove participants");
        }

        ConversationParticipant target = findParticipant(conversation, targetUserId);
        if (target == null) {
            throw new ResourceNotFoundException("Target participant not found in this conversation");
        }

        if (target.getStatus() == ParticipantStatus.LEFT) {
            throw new ConflictException("Participant has already left the conversation");
        }

        target.setStatus(ParticipantStatus.LEFT);
        conversation.removeParticipant(targetUserId);
        conversationRepository.save(conversation);
    }

    /**
     * Allows an authenticated user to leave a conversation.
     */
    @Transactional
    @CacheEvict(value = CacheConstants.CONVERSATIONS_CACHE, key = "'conversation:' + #conversationId")
    public void leaveConversation(String requesterId, String conversationId) {
        Conversation conversation = getConversation(conversationId);

        ConversationParticipant participant = findParticipant(conversation, requesterId);
        if (participant == null || participant.getStatus() == ParticipantStatus.LEFT) {
            throw new ConflictException("You are not an active participant in this conversation");
        }

        if (participant.getRole() == ParticipantRole.OWNER) {
            long ownerCount = conversation.getParticipants().stream()
                    .filter(p -> p.getRole() == ParticipantRole.OWNER && p.getStatus() == ParticipantStatus.ACTIVE)
                    .count();
            if (ownerCount <= 1 && conversation.getParticipants().stream().anyMatch(p -> p.getStatus() == ParticipantStatus.ACTIVE && !requesterId.equals(p.getUserId()))) {
                throw new ForbiddenException("You cannot leave the conversation while being the only OWNER");
            }
        }

        participant.setStatus(ParticipantStatus.LEFT);
        conversation.removeParticipant(requesterId);
        conversationRepository.save(conversation);
    }

    /**
     * Updates a conversation's settings.
     */
    @Transactional
    @CachePut(value = CacheConstants.CONVERSATIONS_CACHE, key = "'conversation:' + #conversationId")
    public Conversation updateConversation(String requesterId, String conversationId, String newName) {
        Conversation conversation = getConversation(conversationId);

        ConversationParticipant requester = findParticipant(conversation, requesterId);
        if (requester == null || (requester.getRole() != ParticipantRole.OWNER && requester.getRole() != ParticipantRole.ADMIN)) {
            throw new ForbiddenException("Only an OWNER or ADMIN can update the conversation");
        }

        if (conversation.getType() == ConversationType.DIRECT) {
            throw new BadRequestException("Cannot rename a direct conversation");
        }

        if (!StringUtils.hasText(newName)) {
            throw new BadRequestException("Conversation name must not be blank");
        }

        String trimmedName = newName.trim();
        if (trimmedName.length() > 100) {
            throw new BadRequestException("Conversation name must not exceed 100 characters");
        }

        conversation.setTitle(trimmedName);
        return conversationRepository.save(conversation);
    }

    /**
     * Archives a conversation.
     */
    @Transactional
    @CacheEvict(value = CacheConstants.CONVERSATIONS_CACHE, key = "'conversation:' + #conversationId")
    public void archiveConversation(String requesterId, String conversationId) {
        Conversation conversation = getConversation(conversationId);

        ConversationParticipant requester = findParticipant(conversation, requesterId);
        if (requester == null || requester.getRole() != ParticipantRole.OWNER) {
            throw new ForbiddenException("Only the OWNER can archive the conversation");
        }

        if (!conversation.isArchived()) {
            conversation.setArchived(true);
            conversationRepository.save(conversation);
        }
    }

    /**
     * Unarchives a conversation.
     */
    @Transactional
    @CacheEvict(value = CacheConstants.CONVERSATIONS_CACHE, key = "'conversation:' + #conversationId")
    public void unarchiveConversation(String requesterId, String conversationId) {
        Conversation conversation = getConversation(conversationId);

        ConversationParticipant requester = findParticipant(conversation, requesterId);
        if (requester == null || requester.getRole() != ParticipantRole.OWNER) {
            throw new ForbiddenException("Only the OWNER can unarchive the conversation");
        }

        if (conversation.isArchived()) {
            conversation.setArchived(false);
            conversationRepository.save(conversation);
        }
    }

    private ConversationParticipant findParticipant(Conversation conversation, String userId) {
        if (conversation == null || conversation.getParticipants() == null) {
            return null;
        }
        return conversation.getParticipants().stream()
                .filter(p -> userId.equals(p.getUserId()))
                .findFirst()
                .orElse(null);
    }
}
