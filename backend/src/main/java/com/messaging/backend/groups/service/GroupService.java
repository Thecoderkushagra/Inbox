package com.messaging.backend.groups.service;

import com.messaging.backend.auth.entity.User;
import com.messaging.backend.auth.repository.UserRepository;
import com.messaging.backend.cache.constants.CacheConstants;
import com.messaging.backend.common.exception.ConflictException;
import com.messaging.backend.common.exception.ForbiddenException;
import com.messaging.backend.common.exception.ResourceNotFoundException;
import com.messaging.backend.groups.dto.response.GroupSocketResponse;
import com.messaging.backend.groups.mapper.GroupMapper;
import com.messaging.backend.messaging.entity.Conversation;
import com.messaging.backend.messaging.entity.ConversationParticipant;
import com.messaging.backend.messaging.enums.ConversationType;
import com.messaging.backend.messaging.enums.ParticipantRole;
import com.messaging.backend.messaging.enums.ParticipantStatus;
import com.messaging.backend.messaging.repository.ConversationRepository;
import com.messaging.backend.notifications.enums.NotificationType;
import com.messaging.backend.notifications.service.NotificationService;
import com.messaging.backend.websocket.constant.WebSocketDestinations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GroupService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final GroupMapper groupMapper;
    private final NotificationService notificationService;

    public GroupService(ConversationRepository conversationRepository,
                        UserRepository userRepository,
                        SimpMessagingTemplate messagingTemplate,
                        GroupMapper groupMapper,
                        NotificationService notificationService) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.groupMapper = groupMapper;
        this.notificationService = notificationService;
    }

    @Transactional
    public Conversation createGroup(String creatorId, String name, String description) {
        User creator = requireUser(creatorId);

        Conversation group = Conversation.builder()
                .type(ConversationType.GROUP)
                .title(name)
                .description(description)
                .archived(false)
                .lastMessageAt(Instant.now())
                .build();

        ConversationParticipant participant = ConversationParticipant.builder()
                .userId(creatorId)
                .role(ParticipantRole.OWNER)
                .status(ParticipantStatus.ACTIVE)
                .joinedAt(Instant.now())
                .build();

        group.addParticipant(participant);
        Conversation saved = conversationRepository.save(group);

        broadcastGroupUpdate(saved);
        return saved;
    }

    @Transactional
    @CacheEvict(value = CacheConstants.GROUPS_CACHE, key = "'group:' + #conversationId")
    public Conversation renameGroup(String conversationId, String requesterId, String newName) {
        Conversation group = requireGroup(conversationId);
        requireAdmin(group, requesterId);

        group.setTitle(newName);
        Conversation saved = conversationRepository.save(group);

        notifyGroupRenamed(saved, requesterId);
        broadcastGroupUpdate(saved);
        return saved;
    }

    @Transactional
    @CacheEvict(value = CacheConstants.GROUPS_CACHE, key = "'group:' + #conversationId")
    public Conversation updateGroupDescription(String conversationId, String requesterId, String newDescription) {
        Conversation group = requireGroup(conversationId);
        requireAdmin(group, requesterId);

        group.setDescription(newDescription);
        Conversation saved = conversationRepository.save(group);
        broadcastGroupUpdate(saved);
        return saved;
    }

    @Transactional
    @CacheEvict(value = CacheConstants.GROUPS_CACHE, key = "'group:' + #conversationId")
    public ConversationParticipant addMember(String conversationId, String requesterId, String targetUserId) {
        Conversation group = requireGroup(conversationId);
        requireAdmin(group, requesterId);
        User targetUser = requireUser(targetUserId);

        if (isMember(group, targetUserId)) {
            throw new ConflictException("User is already a member of this group");
        }

        ConversationParticipant participant = ConversationParticipant.builder()
                .userId(targetUserId)
                .role(ParticipantRole.MEMBER)
                .status(ParticipantStatus.ACTIVE)
                .joinedAt(Instant.now())
                .build();

        group.addParticipant(participant);
        conversationRepository.save(group);

        notificationService.createNotification(targetUserId, NotificationType.GROUP_MEMBER_ADDED, "Added to Group", "You were added to " + group.getTitle(), group.getId());
        broadcastGroupUpdate(group);
        return participant;
    }

    @Transactional
    @CacheEvict(value = CacheConstants.GROUPS_CACHE, key = "'group:' + #conversationId")
    public void removeMember(String conversationId, String requesterId, String targetUserId) {
        Conversation group = requireGroup(conversationId);
        requireAdmin(group, requesterId);

        group.removeParticipant(targetUserId);
        conversationRepository.save(group);

        notificationService.createNotification(targetUserId, NotificationType.GROUP_MEMBER_REMOVED, "Removed from Group", "You were removed from " + group.getTitle(), group.getId());
        broadcastGroupUpdate(group);
    }

    @Transactional
    @CacheEvict(value = CacheConstants.GROUPS_CACHE, key = "'group:' + #conversationId")
    public ConversationParticipant promoteAdmin(String conversationId, String requesterId, String targetUserId) {
        Conversation group = requireGroup(conversationId);
        requireAdmin(group, requesterId);
        ConversationParticipant targetParticipant = requireParticipant(group, targetUserId);

        if (targetParticipant.getRole() == ParticipantRole.ADMIN || targetParticipant.getRole() == ParticipantRole.OWNER) {
            throw new ConflictException("User is already an ADMIN or OWNER");
        }

        targetParticipant.setRole(ParticipantRole.ADMIN);
        conversationRepository.save(group);

        notificationService.createNotification(targetUserId, NotificationType.GROUP_PROMOTED_TO_ADMIN, "Promoted", "You have been promoted to group admin.", group.getId());
        broadcastGroupUpdate(group);
        return targetParticipant;
    }

    @Transactional
    @CacheEvict(value = CacheConstants.GROUPS_CACHE, key = "'group:' + #conversationId")
    public ConversationParticipant demoteAdmin(String conversationId, String requesterId, String targetUserId) {
        Conversation group = requireGroup(conversationId);
        requireAdmin(group, requesterId);
        ConversationParticipant targetParticipant = requireParticipant(group, targetUserId);

        if (targetParticipant.getRole() != ParticipantRole.ADMIN) {
            throw new ConflictException("User is not an ADMIN");
        }

        targetParticipant.setRole(ParticipantRole.MEMBER);
        conversationRepository.save(group);

        notificationService.createNotification(targetUserId, NotificationType.GROUP_DEMOTED_FROM_ADMIN, "Role Updated", "You are no longer a group admin.", group.getId());
        broadcastGroupUpdate(group);
        return targetParticipant;
    }

    @Transactional
    @CacheEvict(value = CacheConstants.GROUPS_CACHE, key = "'group:' + #conversationId")
    public void leaveGroup(String conversationId, String currentUserId) {
        Conversation group = requireGroup(conversationId);
        requireParticipant(group, currentUserId);

        group.removeParticipant(currentUserId);
        if (group.getParticipants().isEmpty()) {
            conversationRepository.delete(group);
        } else {
            conversationRepository.save(group);
            broadcastGroupUpdate(group);
        }
    }

    @Transactional
    @CacheEvict(value = CacheConstants.GROUPS_CACHE, key = "'group:' + #conversationId")
    public void deleteGroup(String conversationId, String requesterId) {
        Conversation group = requireGroup(conversationId);
        requireAdmin(group, requesterId);

        conversationRepository.delete(group);
        broadcastGroupUpdate(group);
    }

    @Transactional(readOnly = true)
    public List<Conversation> getMyGroups(String currentUserId) {
        return conversationRepository.findConversationsByParticipantUserAndStatusAndType(
                currentUserId, ParticipantStatus.ACTIVE, ConversationType.GROUP);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheConstants.GROUPS_CACHE, key = "'group:' + #groupId")
    public Conversation getGroup(String groupId, String currentUserId) {
        Conversation group = requireGroup(groupId);
        requireParticipant(group, currentUserId);
        return group;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheConstants.GROUPS_CACHE, key = "'group:members:' + #groupId")
    public List<ConversationParticipant> getMembers(String groupId, String currentUserId) {
        Conversation group = requireGroup(groupId);
        requireParticipant(group, currentUserId);
        return group.getParticipants().stream()
                .filter(p -> p.getStatus() == ParticipantStatus.ACTIVE)
                .collect(Collectors.toList());
    }

    private Conversation requireGroup(String conversationId) {
        return conversationRepository.findByIdAndType(conversationId, ConversationType.GROUP)
                .orElseThrow(() -> new ResourceNotFoundException("Group conversation not found"));
    }

    private ConversationParticipant requireParticipant(Conversation group, String userId) {
        return group.getParticipants().stream()
                .filter(p -> userId.equals(p.getUserId()) && p.getStatus() == ParticipantStatus.ACTIVE)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("User is not an active member of this group"));
    }

    private boolean isMember(Conversation group, String userId) {
        return group.getParticipantUserIds().contains(userId) ||
                group.getParticipants().stream().anyMatch(p -> userId.equals(p.getUserId()) && p.getStatus() == ParticipantStatus.ACTIVE);
    }

    private void requireAdmin(Conversation group, String userId) {
        ConversationParticipant participant = requireParticipant(group, userId);
        if (participant.getRole() != ParticipantRole.ADMIN && participant.getRole() != ParticipantRole.OWNER) {
            throw new ForbiddenException("Must be an ADMIN or OWNER to perform this action");
        }
    }

    private User requireUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void broadcastGroupUpdate(Conversation group) {
        try {
            GroupSocketResponse response = groupMapper.toSocketResponse(group);
            messagingTemplate.convertAndSend(WebSocketDestinations.GROUP_TOPIC, response);
        } catch (Exception e) {
            log.error("Failed to broadcast group update for group ID: {}", group.getId(), e);
        }
    }

    private void notifyGroupRenamed(Conversation group, String actorId) {
        for (ConversationParticipant participant : group.getParticipants()) {
            if (participant.getStatus() == ParticipantStatus.ACTIVE && !participant.getUserId().equals(actorId)) {
                notificationService.createNotification(participant.getUserId(), NotificationType.GROUP_RENAMED, "Group Updated", "The group name has been changed to " + group.getTitle(), group.getId());
            }
        }
    }
}
