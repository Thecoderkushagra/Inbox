package com.messaging.backend.websocket.controller;

import com.messaging.backend.auth.security.AuthenticatedUser;
import com.messaging.backend.messaging.service.MessageService;
import com.messaging.backend.websocket.constant.WebSocketDestinations;
import com.messaging.backend.websocket.dto.request.SendMessageSocketRequest;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

/**
 * STOMP Controller for handling real-time WebSocket messaging.
 */
@Controller
@Validated
public class MessageWebSocketController {

    private final MessageService messageService;

    public MessageWebSocketController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * Handles inbound STOMP messages.
     * Validates payload, saves to database, and broadcasts to the conversation topic.
     *
     * @param authenticatedUser the principal from the WebSocket session
     * @param request           the validated message payload
     */
    @MessageMapping(WebSocketDestinations.CHAT_SEND)
    public void sendMessage(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid SendMessageSocketRequest request) {

        // Delegate business logic to the core service (which handles database persistence and topic broadcast)
        messageService.sendMessage(
                authenticatedUser.getId(),
                request.conversationId(),
                request.content()
        );
    }
}
