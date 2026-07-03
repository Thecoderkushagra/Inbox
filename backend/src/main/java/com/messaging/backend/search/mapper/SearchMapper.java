package com.messaging.backend.search.mapper;

import com.messaging.backend.auth.entity.User;
import com.messaging.backend.messaging.entity.Conversation;
import com.messaging.backend.messaging.entity.Message;
import com.messaging.backend.search.dto.response.SearchConversationResponse;
import com.messaging.backend.search.dto.response.SearchMessageResponse;
import com.messaging.backend.search.dto.response.SearchSuggestionResponse;
import com.messaging.backend.search.dto.response.SearchUserResponse;
import com.messaging.backend.users.entity.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SearchMapper {

    public SearchUserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }

        String displayName = null;
        String avatarUrl = null;

        UserProfile profile = user.getProfile();
        if (profile != null) {
            displayName = profile.getDisplayName();
            avatarUrl = profile.getAvatarUrl();
        }

        return new SearchUserResponse(
                user.getId(),
                user.getUsername(),
                displayName,
                avatarUrl
        );
    }

    public SearchConversationResponse toConversationResponse(Conversation conversation) {
        if (conversation == null) {
            return null;
        }

        return new SearchConversationResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getDescription(),
                conversation.getType(),
                conversation.getLastMessageAt() != null ? conversation.getLastMessageAt() : conversation.getCreatedAt()
        );
    }

    public SearchMessageResponse toMessageResponse(Message message) {
        if (message == null) {
            return null;
        }

        return new SearchMessageResponse(
                message.getId(),
                message.getConversation() != null ? message.getConversation().getId() : null,
                message.getSender() != null ? message.getSender().getId() : null,
                message.getContent(),
                message.getCreatedAt(),
                message.isEdited()
        );
    }

    public Page<SearchUserResponse> toUserResponsePage(Page<User> page) {
        if (page == null) {
            return null;
        }
        return page.map(this::toUserResponse);
    }

    public Page<SearchConversationResponse> toConversationResponsePage(Page<Conversation> page) {
        if (page == null) {
            return null;
        }
        return page.map(this::toConversationResponse);
    }

    public Page<SearchMessageResponse> toMessageResponsePage(Page<Message> page) {
        if (page == null) {
            return null;
        }
        return page.map(this::toMessageResponse);
    }

    public List<SearchUserResponse> toUserResponseList(List<User> users) {
        if (users == null) {
            return Collections.emptyList();
        }
        return users.stream().map(this::toUserResponse).collect(Collectors.toList());
    }

    public List<SearchConversationResponse> toConversationResponseList(List<Conversation> conversations) {
        if (conversations == null) {
            return Collections.emptyList();
        }
        return conversations.stream().map(this::toConversationResponse).collect(Collectors.toList());
    }

    public SearchSuggestionResponse toSuggestionResponse(List<User> users, List<Conversation> groups) {
        return new SearchSuggestionResponse(
                toUserResponseList(users),
                toConversationResponseList(groups)
        );
    }
}
