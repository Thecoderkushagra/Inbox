package com.messaging.backend.messaging.mapper;

import com.messaging.backend.auth.entity.User;
import com.messaging.backend.auth.service.AuthService;
import com.messaging.backend.messaging.dto.response.ConversationParticipantResponse;
import com.messaging.backend.messaging.dto.response.ConversationResponse;
import com.messaging.backend.messaging.entity.Conversation;
import com.messaging.backend.messaging.entity.ConversationParticipant;
import com.messaging.backend.messaging.enums.ConversationType;
import com.messaging.backend.users.entity.UserProfile;
import com.messaging.backend.users.service.UserProfileService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Conversation MongoDB documents and DTOs.
 */
@Component
public class ConversationMapper {

    private final AuthService authService;
    private final UserProfileService userProfileService;

    public ConversationMapper(@Lazy AuthService authService, @Lazy UserProfileService userProfileService) {
        this.authService = authService;
        this.userProfileService = userProfileService;
    }

    /**
     * Converts a Conversation document to a ConversationResponse DTO with requester-specific title resolution.
     */
    public ConversationResponse toConversationResponse(Conversation conversation, String requesterId) {
        if (conversation == null) {
            return null;
        }

        List<ConversationParticipantResponse> participantResponses = toParticipantResponseList(
                conversation.getParticipants() != null ? conversation.getParticipants().stream().toList() : Collections.emptyList()
        );

        String title = conversation.getTitle();
        if (conversation.getType() == ConversationType.DIRECT && requesterId != null) {
            for (ConversationParticipantResponse p : participantResponses) {
                if (!requesterId.equals(p.userId())) {
                    if (p.displayName() != null && !p.displayName().isBlank()) {
                        title = p.displayName();
                    } else if (p.username() != null && !p.username().isBlank()) {
                        title = p.username();
                    }
                    break;
                }
            }
        }

        return new ConversationResponse(
                conversation.getId(),
                conversation.getType(),
                title,
                conversation.getDescription(),
                conversation.isArchived(),
                conversation.getLastMessageAt(),
                conversation.getCreatedAt(),
                participantResponses
        );
    }

    /**
     * Converts a Conversation document to a ConversationResponse DTO.
     */
    public ConversationResponse toConversationResponse(Conversation conversation) {
        return toConversationResponse(conversation, null);
    }

    /**
     * Converts a ConversationParticipant to a ConversationParticipantResponse DTO.
     */
    public ConversationParticipantResponse toParticipantResponse(ConversationParticipant participant) {
        if (participant == null) {
            return null;
        }

        String username = null;
        String displayName = null;
        String avatarUrl = null;

        if (participant.getUserId() != null) {
            try {
                User user = authService.getUserById(participant.getUserId());
                if (user != null) {
                    username = user.getUsername();
                }
            } catch (Exception ignored) {
            }

            try {
                UserProfile profile = userProfileService.getProfileByUserId(participant.getUserId());
                if (profile != null) {
                    displayName = profile.getDisplayName();
                    avatarUrl = profile.getAvatarUrl();
                }
            } catch (Exception ignored) {
            }
        }

        return new ConversationParticipantResponse(
                participant.getUserId(),
                username,
                displayName,
                avatarUrl,
                participant.getRole(),
                participant.getStatus(),
                participant.getJoinedAt()
        );
    }

    /**
     * Converts a list of ConversationParticipant to a list of ConversationParticipantResponse DTOs.
     */
    public List<ConversationParticipantResponse> toParticipantResponseList(List<ConversationParticipant> participants) {
        if (participants == null) {
            return Collections.emptyList();
        }
        return participants.stream()
                .map(this::toParticipantResponse)
                .collect(Collectors.toList());
    }
}
