package com.messaging.backend.search.dto.response;

import java.util.List;

public record GlobalSearchResponse(
        List<SearchUserResponse> users,
        List<SearchConversationResponse> groups
) {}
