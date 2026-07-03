package com.messaging.backend.search.websocket;

import com.messaging.backend.auth.security.AuthenticatedUser;
import com.messaging.backend.search.dto.response.SearchSuggestionResponse;
import com.messaging.backend.search.mapper.SearchMapper;
import com.messaging.backend.search.service.SearchService;
import com.messaging.backend.websocket.constant.WebSocketDestinations;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
public class SearchWebSocketController {

    private final SearchService searchService;
    private final SearchMapper searchMapper;
    private final SimpMessagingTemplate messagingTemplate;

    public SearchWebSocketController(SearchService searchService, SearchMapper searchMapper, SimpMessagingTemplate messagingTemplate) {
        this.searchService = searchService;
        this.searchMapper = searchMapper;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping(WebSocketDestinations.SEARCH_SUGGESTIONS_APP)
    public void getSuggestions(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Payload String keyword) {

        SearchService.SuggestionResult result = searchService.getSuggestions(currentUser.getId(), keyword);
        SearchSuggestionResponse response = searchMapper.toSuggestionResponse(result.users(), result.groups());

        messagingTemplate.convertAndSendToUser(
                currentUser.getId().toString(),
                WebSocketDestinations.SEARCH_SUGGESTIONS_QUEUE,
                response
        );
    }
}
