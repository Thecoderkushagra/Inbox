package com.messaging.backend.search.controller;

import com.messaging.backend.auth.security.AuthenticatedUser;
import com.messaging.backend.common.dto.response.SuccessResponse;
import com.messaging.backend.search.dto.response.GlobalSearchResponse;
import com.messaging.backend.search.dto.response.SearchConversationResponse;
import com.messaging.backend.search.dto.response.SearchMessageResponse;
import com.messaging.backend.search.dto.response.SearchUserResponse;
import com.messaging.backend.search.mapper.SearchMapper;
import com.messaging.backend.search.service.SearchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final SearchService searchService;
    private final SearchMapper searchMapper;

    public SearchController(SearchService searchService, SearchMapper searchMapper) {
        this.searchService = searchService;
        this.searchMapper = searchMapper;
    }

    @GetMapping("/users")
    public SuccessResponse<Page<SearchUserResponse>> searchUsers(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam("keyword") String keyword,
            Pageable pageable) {

        Page<SearchUserResponse> result = searchMapper.toUserResponsePage(
                searchService.searchUsers(currentUser.getId(), keyword, pageable)
        );

        return SuccessResponse.success("Users retrieved successfully", result);
    }

    @GetMapping("/friends")
    public SuccessResponse<Page<SearchUserResponse>> searchFriends(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam("keyword") String keyword,
            Pageable pageable) {

        Page<SearchUserResponse> result = searchMapper.toUserResponsePage(
                searchService.searchFriends(currentUser.getId(), keyword, pageable)
        );

        return SuccessResponse.success("Friends retrieved successfully", result);
    }

    @GetMapping("/conversations")
    public SuccessResponse<Page<SearchConversationResponse>> searchConversations(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam("keyword") String keyword,
            Pageable pageable) {

        Page<SearchConversationResponse> result = searchMapper.toConversationResponsePage(
                searchService.searchConversations(currentUser.getId(), keyword, pageable)
        );

        return SuccessResponse.success("Conversations retrieved successfully", result);
    }

    @GetMapping("/groups")
    public SuccessResponse<Page<SearchConversationResponse>> searchGroups(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam("keyword") String keyword,
            Pageable pageable) {

        Page<SearchConversationResponse> result = searchMapper.toConversationResponsePage(
                searchService.searchGroups(currentUser.getId(), keyword, pageable)
        );

        return SuccessResponse.success("Groups retrieved successfully", result);
    }

    @GetMapping("/messages")
    public SuccessResponse<Page<SearchMessageResponse>> searchMessages(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam("conversationId") UUID conversationId,
            @RequestParam("keyword") String keyword,
            Pageable pageable) {

        Page<SearchMessageResponse> result = searchMapper.toMessageResponsePage(
                searchService.searchMessages(currentUser.getId(), conversationId, keyword, pageable)
        );

        return SuccessResponse.success("Messages retrieved successfully", result);
    }

    @GetMapping("/global")
    public SuccessResponse<GlobalSearchResponse> globalSearch(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam("keyword") String keyword) {

        Pageable limitedPage = PageRequest.of(0, 5);

        var users = searchMapper.toUserResponseList(
                searchService.searchUsers(currentUser.getId(), keyword, limitedPage).getContent()
        );

        var groups = searchMapper.toConversationResponseList(
                searchService.searchGroups(currentUser.getId(), keyword, limitedPage).getContent()
        );

        GlobalSearchResponse response = new GlobalSearchResponse(users, groups);

        return SuccessResponse.success("Global search completed successfully", response);
    }
}
