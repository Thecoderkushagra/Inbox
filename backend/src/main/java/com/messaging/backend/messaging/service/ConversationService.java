package com.messaging.backend.messaging.service;

import com.messaging.backend.auth.entity.User;
import com.messaging.backend.common.exception.BadRequestException;
import com.messaging.backend.common.exception.ConflictException;
import com.messaging.backend.common.exception.ForbiddenException;
import com.messaging.backend.common.exception.ResourceNotFoundException;
import com.messaging.backend.messaging.entity.Conversation;
import com.messaging.backend.messaging.entity.ConversationParticipant;
import com.messaging.backend.messaging.enums.ConversationType;
import com.messaging.backend.messaging.enums.ParticipantRole;
import com.messaging.backend.messaging.enums.ParticipantStatus;
import com.messaging.backend.messaging.repository.ConversationParticipantRepository;
import com.messaging.backend.messaging.repository.ConversationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.UUID;

/**
 * Service for managing conversations and participants.
 *
 * <p>Purpose:
 * Handles business logic for creating and retrieving conversations and their members.
 */
@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository conversationParticipantRepository;

    public ConversationService(ConversationRepository conversationRepository,
                               ConversationParticipantRepository conversationParticipantRepository) {
        this.conversationRepository = conversationRepository;
        this.conversationParticipantRepository = conversationParticipantRepository;
    }

    /**
     * Creates a one-to-one private conversation between two users.
     *
     * @param creator the user creating the conversation
     * @param recipient the user receiving the conversation
     * @return the created Conversation
     * @throws BadRequestException if either user is null or if they are the same user
     */
    @Transactional
    public Conversation createPrivateConversation(User creator, User recipient) {
        if (creator == null || recipient == null) {
            throw new BadRequestException("Creator and recipient must not be null");
        }
        if (creator.getId().equals(recipient.getId())) {
            throw new BadRequestException("Creator and recipient cannot be the same user");
        }

        Conversation conversation = Conversation.builder()
                .type(ConversationType.DIRECT)
                .archived(false)
                .build();
        
        Conversation savedConversation = conversationRepository.save(conversation);

        ConversationParticipant creatorParticipant = ConversationParticipant.builder()
                .conversation(savedConversation)
                .user(creator)
                .role(ParticipantRole.OWNER)
                .status(ParticipantStatus.ACTIVE)
                .joinedAt(Instant.now())
                .build();

        ConversationParticipant recipientParticipant = ConversationParticipant.builder()
                .conversation(savedConversation)
                .user(recipient)
                .role(ParticipantRole.MEMBER)
                .status(ParticipantStatus.ACTIVE)
                .joinedAt(Instant.now())
                .build();

        conversationParticipantRepository.save(creatorParticipant);
        conversationParticipantRepository.save(recipientParticipant);

        return savedConversation;
    }

    /**
     * Creates a new group conversation.
     *
     * @param owner the user creating the group
     * @param name the name of the group
     * @return the created Conversation
     * @throws BadRequestException if the owner is null, or if the name is blank or too long
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
                .build();

        Conversation savedConversation = conversationRepository.save(conversation);

        ConversationParticipant ownerParticipant = ConversationParticipant.builder()
                .conversation(savedConversation)
                .user(owner)
                .role(ParticipantRole.OWNER)
                .status(ParticipantStatus.ACTIVE)
                .joinedAt(Instant.now())
                .build();

        conversationParticipantRepository.save(ownerParticipant);

        return savedConversation;
    }

    /**
     * Retrieves a conversation by its ID.
     *
     * @param conversationId the ID of the conversation
     * @return the Conversation
     * @throws ResourceNotFoundException if the conversation does not exist
     */
    @Transactional(readOnly = true)
    public Conversation getConversation(UUID conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
    }

    /**
     * Retrieves a conversation and validates that the user is a member of it.
     *
     * @param conversationId the ID of the conversation
     * @param userId the ID of the user
     * @return the Conversation
     * @throws ResourceNotFoundException if the conversation does not exist
     * @throws ForbiddenException if the user is not a participant
     */
    @Transactional(readOnly = true)
    public Conversation getConversationForUser(UUID conversationId, UUID userId) {
        Conversation conversation = getConversation(conversationId);
        if (!isParticipant(conversationId, userId)) {
            throw new ForbiddenException("User is not a participant in this conversation");
        }
        return conversation;
    }

    /**
     * Retrieves the participants of a conversation with pagination.
     *
     * @param conversationId the ID of the conversation
     * @param pageable the pagination parameters
     * @return a page of ConversationParticipant
     */
    @Transactional(readOnly = true)
    public Page<ConversationParticipant> getParticipants(UUID conversationId, Pageable pageable) {
        return conversationParticipantRepository.findByConversationId(conversationId, pageable);
    }

    /**
     * Checks if a user is a participant in a conversation.
     *
     * @param conversationId the ID of the conversation
     * @param userId the ID of the user
     * @return true if the user is a participant, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isParticipant(UUID conversationId, UUID userId) {
        return conversationParticipantRepository.existsByConversationIdAndUserId(conversationId, userId);
    }

    /**
     * Adds a new participant to an existing conversation.
     *
     * @param requesterId the ID of the user making the request
     * @param conversationId the ID of the conversation
     * @param newParticipant the new User to be added
     * @return the created ConversationParticipant
     * @throws ResourceNotFoundException if the conversation does not exist
     * @throws ForbiddenException if the requester is not an OWNER
     * @throws ConflictException if the new participant is already in the conversation
     */
    @Transactional
    public ConversationParticipant addParticipant(UUID requesterId, UUID conversationId, User newParticipant) {
        Conversation conversation = getConversation(conversationId);

        ConversationParticipant requester = conversationParticipantRepository
                .findByConversationIdAndUserId(conversationId, requesterId)
                .orElseThrow(() -> new ForbiddenException("Requester is not a participant"));

        if (requester.getRole() != ParticipantRole.OWNER) {
            throw new ForbiddenException("Only the OWNER can add participants");
        }

        if (isParticipant(conversationId, newParticipant.getId())) {
            throw new ConflictException("User is already a participant in this conversation");
        }

        ConversationParticipant participant = ConversationParticipant.builder()
                .conversation(conversation)
                .user(newParticipant)
                .role(ParticipantRole.MEMBER)
                .status(ParticipantStatus.ACTIVE)
                .joinedAt(Instant.now())
                .build();

        return conversationParticipantRepository.save(participant);
    }

    /**
     * Removes a participant from an existing conversation.
     *
     * @param requesterId the ID of the user making the request
     * @param conversationId the ID of the conversation
     * @param targetUserId the ID of the user to be removed
     * @throws ResourceNotFoundException if the conversation or target participant does not exist
     * @throws ForbiddenException if the requester is not an OWNER or tries to remove themselves
     * @throws ConflictException if the target participant has already left
     */
    @Transactional
    public void removeParticipant(UUID requesterId, UUID conversationId, UUID targetUserId) {
        getConversation(conversationId);

        if (requesterId.equals(targetUserId)) {
            throw new ForbiddenException("Owner cannot remove themselves. Use the leave endpoint instead.");
        }

        ConversationParticipant requester = conversationParticipantRepository
                .findByConversationIdAndUserId(conversationId, requesterId)
                .orElseThrow(() -> new ForbiddenException("Requester is not a participant"));

        if (requester.getRole() != ParticipantRole.OWNER) {
            throw new ForbiddenException("Only the OWNER can remove participants");
        }

        ConversationParticipant target = conversationParticipantRepository
                .findByConversationIdAndUserId(conversationId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Target participant not found in this conversation"));

        if (target.getStatus() == ParticipantStatus.LEFT) {
            throw new ConflictException("Participant has already left the conversation");
        }

        target.setStatus(ParticipantStatus.LEFT);
        conversationParticipantRepository.save(target);
    }

    /**
     * Allows an authenticated user to leave a conversation.
     *
     * @param requesterId the ID of the user leaving
     * @param conversationId the ID of the conversation
     * @throws ResourceNotFoundException if the conversation or participant does not exist
     * @throws ForbiddenException if the user is the only OWNER
     * @throws ConflictException if the user has already left
     */
    @Transactional
    public void leaveConversation(UUID requesterId, UUID conversationId) {
        getConversation(conversationId);

        ConversationParticipant participant = conversationParticipantRepository
                .findByConversationIdAndUserId(conversationId, requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Requester is not a participant"));

        if (participant.getStatus() == ParticipantStatus.LEFT) {
            throw new ConflictException("You have already left this conversation");
        }

        if (participant.getRole() == ParticipantRole.OWNER) {
            Page<ConversationParticipant> owners = conversationParticipantRepository
                    .findByConversationIdAndRole(conversationId, ParticipantRole.OWNER, Pageable.unpaged());
            
            if (owners.getTotalElements() <= 1) {
                throw new ForbiddenException("You cannot leave the conversation while being the only OWNER");
            }
        }

        participant.setStatus(ParticipantStatus.LEFT);
        conversationParticipantRepository.save(participant);
    }

    /**
     * Updates a conversation's settings (e.g., renaming a group).
     *
     * @param requesterId the ID of the user making the request
     * @param conversationId the ID of the conversation
     * @param newName the new name for the conversation
     * @return the updated Conversation
     * @throws ResourceNotFoundException if the conversation does not exist
     * @throws ForbiddenException if the requester is not an OWNER
     * @throws BadRequestException if the conversation is DIRECT or the name is invalid
     */
    @Transactional
    public Conversation updateConversation(UUID requesterId, UUID conversationId, String newName) {
        Conversation conversation = getConversation(conversationId);

        ConversationParticipant requester = conversationParticipantRepository
                .findByConversationIdAndUserId(conversationId, requesterId)
                .orElseThrow(() -> new ForbiddenException("Requester is not a participant"));

        if (requester.getRole() != ParticipantRole.OWNER) {
            throw new ForbiddenException("Only the OWNER can update the conversation");
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
     *
     * @param requesterId the ID of the user making the request
     * @param conversationId the ID of the conversation
     * @throws ResourceNotFoundException if the conversation does not exist
     * @throws ForbiddenException if the requester is not an OWNER
     */
    @Transactional
    public void archiveConversation(UUID requesterId, UUID conversationId) {
        Conversation conversation = getConversation(conversationId);

        ConversationParticipant requester = conversationParticipantRepository
                .findByConversationIdAndUserId(conversationId, requesterId)
                .orElseThrow(() -> new ForbiddenException("Requester is not a participant"));

        if (requester.getRole() != ParticipantRole.OWNER) {
            throw new ForbiddenException("Only the OWNER can archive the conversation");
        }

        if (!conversation.isArchived()) {
            conversation.setArchived(true);
            conversationRepository.save(conversation);
        }
    }

    /**
     * Unarchives a conversation.
     *
     * @param requesterId the ID of the user making the request
     * @param conversationId the ID of the conversation
     * @throws ResourceNotFoundException if the conversation does not exist
     * @throws ForbiddenException if the requester is not an OWNER
     */
    @Transactional
    public void unarchiveConversation(UUID requesterId, UUID conversationId) {
        Conversation conversation = getConversation(conversationId);

        ConversationParticipant requester = conversationParticipantRepository
                .findByConversationIdAndUserId(conversationId, requesterId)
                .orElseThrow(() -> new ForbiddenException("Requester is not a participant"));

        if (requester.getRole() != ParticipantRole.OWNER) {
            throw new ForbiddenException("Only the OWNER can unarchive the conversation");
        }

        if (conversation.isArchived()) {
            conversation.setArchived(false);
            conversationRepository.save(conversation);
        }
    }
}
