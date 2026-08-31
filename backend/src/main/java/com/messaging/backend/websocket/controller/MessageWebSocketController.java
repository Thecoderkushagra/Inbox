package com.messaging.backend.websocket.controller;

import com.messaging.backend.auth.security.AuthenticatedUser;
import com.messaging.backend.messaging.entity.Message;
import com.messaging.backend.messaging.mapper.MessageMapper;
import com.messaging.backend.messaging.service.MessageService;
import com.messaging.backend.websocket.constant.WebSocketDestinations;
import com.messaging.backend.websocket.dto.request.SendMessageSocketRequest;
import com.messaging.backend.websocket.dto.response.MessageSocketResponse;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final MessageMapper messageMapper;
    private final SimpMessagingTemplate messagingTemplate;

    public MessageWebSocketController(MessageService messageService, 
                                      MessageMapper messageMapper, 
                                      SimpMessagingTemplate messagingTemplate) {
        this.messageService = messageService;
        this.messageMapper = messageMapper;
        this.messagingTemplate = messagingTemplate;
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

        // Delegate business logic to the core service
        Message message = messageService.sendMessage(
                authenticatedUser.getId(),
                request.conversationId(),
                request.content()
        );

        // Map strictly to the outbound STOMP DTO
        MessageSocketResponse response = messageMapper.toSocketResponse(message);

        // Construct target topic and broadcast
        String destination = WebSocketDestinations.CHAT_TOPIC + request.conversationId();
        messagingTemplate.convertAndSend(destination, response);
    }
}
