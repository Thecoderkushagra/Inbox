package com.messaging.backend.groups.service;

import com.messaging.backend.auth.entity.User;
import com.messaging.backend.auth.repository.UserRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.messaging.backend.groups.mapper.GroupMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.messaging.backend.websocket.constant.WebSocketDestinations;

@Service
public class GroupService {

    private static final Logger log = LoggerFactory.getLogger(GroupService.class);

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository conversationParticipantRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final GroupMapper groupMapper;

    public GroupService(ConversationRepository conversationRepository,
                        ConversationParticipantRepository conversationParticipantRepository,
                        UserRepository userRepository,
                        SimpMessagingTemplate messagingTemplate,
                        GroupMapper groupMapper) {
        this.conversationRepository = conversationRepository;
        this.conversationParticipantRepository = conversationParticipantRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.groupMapper = groupMapper;
    }

    @Transactional
    public Conversation createGroup(UUID creatorId, String name, String description) {
        User creator = requireUser(creatorId);

        Conversation group = Conversation.builder()
                .type(ConversationType.GROUP)
                .title(name)
                .description(description)
                .archived(false)
                .build();

        group = conversationRepository.save(group);

        ConversationParticipant participant = ConversationParticipant.builder()
                .conversation(group)
                .user(creator)
                .role(ParticipantRole.ADMIN)
                .status(ParticipantStatus.ACTIVE)
                .joinedAt(Instant.now())
                .muted(false)
                .pinned(false)
                .build();

        conversationParticipantRepository.save(participant);

        broadcastGroupUpdate(group);
        return group;
    }

    @Transactional
    public Conversation renameGroup(UUID conversationId, UUID requesterId, String newName) {
        Conversation group = requireGroup(conversationId);
        requireAdmin(conversationId, requesterId);

        group.setTitle(newName);
        group = conversationRepository.save(group);
        broadcastGroupUpdate(group);
        return group;
    }

    @Transactional
    public Conversation updateGroupDescription(UUID conversationId, UUID requesterId, String newDescription) {
        Conversation group = requireGroup(conversationId);
        requireAdmin(conversationId, requesterId);

        group.setDescription(newDescription);
        group = conversationRepository.save(group);
        broadcastGroupUpdate(group);
        return group;
    }

    @Transactional
    public ConversationParticipant addMember(UUID conversationId, UUID requesterId, UUID targetUserId) {
        Conversation group = requireGroup(conversationId);
        requireAdmin(conversationId, requesterId);
        User targetUser = requireUser(targetUserId);

        if (conversationParticipantRepository.existsByConversationIdAndUserId(conversationId, targetUserId)) {
            throw new ConflictException("User is already a member of this group");
        }

        ConversationParticipant participant = ConversationParticipant.builder()
                .conversation(group)
                .user(targetUser)
                .role(ParticipantRole.MEMBER)
                .status(ParticipantStatus.ACTIVE)
                .joinedAt(Instant.now())
                .muted(false)
                .pinned(false)
                .build();

        participant = conversationParticipantRepository.save(participant);
        broadcastGroupUpdate(group);
        return participant;
    }

    @Transactional
    public void removeMember(UUID conversationId, UUID requesterId, UUID targetUserId) {
        Conversation group = requireGroup(conversationId);
        requireAdmin(conversationId, requesterId);
        ConversationParticipant targetParticipant = requireParticipant(conversationId, targetUserId);

        conversationParticipantRepository.delete(targetParticipant);
        broadcastGroupUpdate(group);
    }

    @Transactional
    public ConversationParticipant promoteAdmin(UUID conversationId, UUID requesterId, UUID targetUserId) {
        Conversation group = requireGroup(conversationId);
        requireAdmin(conversationId, requesterId);
        ConversationParticipant targetParticipant = requireParticipant(conversationId, targetUserId);

        if (targetParticipant.getRole() == ParticipantRole.ADMIN || targetParticipant.getRole() == ParticipantRole.OWNER) {
            throw new ConflictException("User is already an ADMIN");
        }

        targetParticipant.setRole(ParticipantRole.ADMIN);
        targetParticipant = conversationParticipantRepository.save(targetParticipant);
        broadcastGroupUpdate(group);
        return targetParticipant;
    }

    @Transactional
    public ConversationParticipant demoteAdmin(UUID conversationId, UUID requesterId, UUID targetUserId) {
        Conversation group = requireGroup(conversationId);
        requireAdmin(conversationId, requesterId);
        ConversationParticipant targetParticipant = requireParticipant(conversationId, targetUserId);

        if (targetParticipant.getRole() != ParticipantRole.ADMIN) {
            throw new ConflictException("User is not an ADMIN");
        }

        List<ConversationParticipant> admins = conversationParticipantRepository.findByConversationIdAndRole(conversationId, ParticipantRole.ADMIN);
        if (admins.size() <= 1) {
            throw new ConflictException("Cannot demote the last admin");
        }

        targetParticipant.setRole(ParticipantRole.MEMBER);
        targetParticipant = conversationParticipantRepository.save(targetParticipant);
        broadcastGroupUpdate(group);
        return targetParticipant;
    }

    @Transactional
    public void leaveGroup(UUID conversationId, UUID currentUserId) {
        Conversation group = requireGroup(conversationId);
        ConversationParticipant participant = requireParticipant(conversationId, currentUserId);

        long memberCount = conversationParticipantRepository.countByConversationId(conversationId);

        if (memberCount <= 1) {
            conversationRepository.delete(group);
            return;
        }

        if (participant.getRole() == ParticipantRole.ADMIN || participant.getRole() == ParticipantRole.OWNER) {
            List<ConversationParticipant> admins = conversationParticipantRepository.findByConversationIdAndRole(conversationId, ParticipantRole.ADMIN);
            if (admins.size() == 1) {
                List<ConversationParticipant> allMembers = conversationParticipantRepository.findByConversationId(conversationId);
                ConversationParticipant nextAdmin = allMembers.stream()
                        .filter(p -> !p.getUser().getId().equals(currentUserId))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Expected other members to exist"));
                
                nextAdmin.setRole(ParticipantRole.ADMIN);
                conversationParticipantRepository.save(nextAdmin);
            }
        }

        conversationParticipantRepository.delete(participant);
        broadcastGroupUpdate(group);
    }

    @Transactional
    public void deleteGroup(UUID conversationId, UUID requesterId) {
        Conversation group = requireGroup(conversationId);
        requireAdmin(conversationId, requesterId);

        conversationRepository.delete(group);
        broadcastGroupUpdate(group);
    }

    @Transactional(readOnly = true)
    public List<Conversation> getMyGroups(UUID currentUserId) {
        return conversationRepository.findConversationsByParticipantUserAndStatusAndType(
                currentUserId, ParticipantStatus.ACTIVE, ConversationType.GROUP);
    }

    @Transactional(readOnly = true)
    public Conversation getGroup(UUID groupId, UUID currentUserId) {
        Conversation group = requireGroup(groupId);
        requireParticipant(groupId, currentUserId);
        return group;
    }

    @Transactional(readOnly = true)
    public List<ConversationParticipant> getMembers(UUID groupId, UUID currentUserId) {
        requireGroup(groupId);
        requireParticipant(groupId, currentUserId);
        return conversationParticipantRepository.findByConversationIdAndStatus(groupId, ParticipantStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    private Conversation requireGroup(UUID conversationId) {
        return conversationRepository.findByIdAndType(conversationId, ConversationType.GROUP)
                .orElseThrow(() -> new ResourceNotFoundException("Group conversation not found"));
    }

    @Transactional(readOnly = true)
    private ConversationParticipant requireParticipant(UUID conversationId, UUID userId) {
        return conversationParticipantRepository.findByConversationIdAndUserId(conversationId, userId)
                .filter(p -> p.getStatus() == ParticipantStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User is not an active member of this group"));
    }

    @Transactional(readOnly = true)
    private void requireAdmin(UUID conversationId, UUID userId) {
        ConversationParticipant participant = requireParticipant(conversationId, userId);
        if (participant.getRole() != ParticipantRole.ADMIN && participant.getRole() != ParticipantRole.OWNER) {
            throw new ForbiddenException("Must be an ADMIN to perform this action");
        }
    }

    @Transactional(readOnly = true)
    private User requireUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void broadcastGroupUpdate(Conversation group) {
        try {
            com.messaging.backend.groups.dto.response.GroupSocketResponse response = groupMapper.toSocketResponse(group);
            messagingTemplate.convertAndSend(WebSocketDestinations.GROUP_TOPIC, response);
        } catch (Exception e) {
            log.error("Failed to broadcast group update for group ID: {}", group.getId(), e);
        }
    }
}
