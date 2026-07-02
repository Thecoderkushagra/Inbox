package com.messaging.backend.websocket;

import com.messaging.backend.auth.entity.User;
import com.messaging.backend.auth.enums.UserStatus;
import com.messaging.backend.auth.repository.UserRepository;
import com.messaging.backend.common.security.jwt.JwtTokenProvider;
import com.messaging.backend.messaging.entity.Conversation;
import com.messaging.backend.messaging.enums.ConversationType;
import com.messaging.backend.messaging.repository.ConversationRepository;
import com.messaging.backend.messaging.repository.MessageRepository;
import com.messaging.backend.messaging.service.ConversationService;
import com.messaging.backend.websocket.constant.WebSocketDestinations;
import com.messaging.backend.websocket.dto.request.SendMessageSocketRequest;
import com.messaging.backend.websocket.dto.response.MessageSocketResponse;
import com.messaging.backend.websocket.dto.response.WebSocketErrorResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.messaging.backend.common.support.BaseIntegrationTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@org.springframework.test.context.TestPropertySource(properties = {
    "APP_NAME=MessagingTest",
    "JWT_SECRET=thisismysecretkeywhichneedsToBeLongEnoughForHS256Algorithm",
    "JWT_ACCESS_EXPIRATION=3600000",
    "JWT_REFRESH_EXPIRATION=86400000"
})
@ActiveProfiles("test")
public class MessageWebSocketIntegrationTest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    private WebSocketStompClient stompClient;

    private User user1;
    private User user2;
    private User user3;
    private Conversation conversation;
    private String jwt1;
    private String jwt2;
    private String jwt3;

    @BeforeEach
    void setUp() {
        messageRepository.deleteAll();
        conversationRepository.deleteAll();
        userRepository.deleteAll();

        stompClient = new WebSocketStompClient(new SockJsClient(
                List.of(new WebSocketTransport(new StandardWebSocketClient()))));
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        stompClient.setMessageConverter(converter);

        user1 = userRepository.save(createUser("user1@test.com", "user1"));
        user2 = userRepository.save(createUser("user2@test.com", "user2"));
        user3 = userRepository.save(createUser("user3@test.com", "user3"));

        conversation = conversationService.createPrivateConversation(user1, user2);

        jwt1 = jwtTokenProvider.generateAccessToken(user1.getUsername(), List.of("ROLE_USER"));
        jwt2 = jwtTokenProvider.generateAccessToken(user2.getUsername(), List.of("ROLE_USER"));
        jwt3 = jwtTokenProvider.generateAccessToken(user3.getUsername(), List.of("ROLE_USER"));
    }

    @AfterEach
    void tearDown() {
        messageRepository.deleteAll();
        conversationRepository.deleteAll();
        userRepository.deleteAll();
        if (stompClient != null) {
            stompClient.stop();
        }
    }

    private User createUser(String email, String username) {
        return new User(email, username, "hash", UserStatus.ACTIVE, true);
    }

    private StompSession connect(String jwt) throws Exception {
        StompHeaders headers = new StompHeaders();
        if (jwt != null) {
            headers.add("Authorization", "Bearer " + jwt);
        }
        return stompClient.connectAsync("ws://localhost:" + port + "/ws", new WebSocketHttpHeaders(), headers, new StompSessionHandlerAdapter() {}).get(5, TimeUnit.SECONDS);
    }

    @Test
    void testAuthentication_MissingJwt_ConnectionRejected() {
        assertThrows(ExecutionException.class, () -> connect(null));
    }

    @Test
    void testAuthentication_MalformedJwt_ConnectionRejected() {
        assertThrows(ExecutionException.class, () -> connect("invalid.token"));
    }

    @Test
    void testMessageSendingAndBroadcasting_Success() throws Exception {
        StompSession session1 = connect(jwt1);
        StompSession session2 = connect(jwt2);

        CompletableFuture<MessageSocketResponse> responseFuture = new CompletableFuture<>();

        // session2 subscribes to conversation
        session2.subscribe(WebSocketDestinations.CHAT_TOPIC + conversation.getId(), new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return MessageSocketResponse.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                responseFuture.complete((MessageSocketResponse) payload);
            }
        });

        // Give subscription a moment to register
        Thread.sleep(500);

        // session1 sends a message
        SendMessageSocketRequest request = new SendMessageSocketRequest(conversation.getId(), "Hello WebSocket");
        session1.send(WebSocketDestinations.APP_PREFIX + WebSocketDestinations.CHAT_SEND, request);

        // Wait for response
        MessageSocketResponse response = responseFuture.get(5, TimeUnit.SECONDS);

        assertThat(response).isNotNull();
        assertThat(response.content()).isEqualTo("Hello WebSocket");
        assertThat(response.senderId()).isEqualTo(user1.getId());
        assertThat(response.conversationId()).isEqualTo(conversation.getId());
        assertThat(response.id()).isNotNull();

        // Verify persistence
        boolean exists = messageRepository.findById(response.id()).isPresent();
        assertThat(exists).isTrue();
    }

    @Test
    void testAuthorization_UserNotInConversation_ForbiddenException() throws Exception {
        StompSession session3 = connect(jwt3);

        CompletableFuture<WebSocketErrorResponse> errorFuture = new CompletableFuture<>();

        // session3 subscribes to errors
        session3.subscribe(WebSocketDestinations.USER_QUEUE_PREFIX + WebSocketDestinations.USER_ERROR_QUEUE, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return WebSocketErrorResponse.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                errorFuture.complete((WebSocketErrorResponse) payload);
            }
        });

        Thread.sleep(500);

        // user3 (not in conversation) tries to send a message
        SendMessageSocketRequest request = new SendMessageSocketRequest(conversation.getId(), "Hacking...");
        session3.send(WebSocketDestinations.APP_PREFIX + WebSocketDestinations.CHAT_SEND, request);

        WebSocketErrorResponse error = errorFuture.get(5, TimeUnit.SECONDS);

        assertThat(error).isNotNull();
        assertThat(error.error()).isEqualTo("Forbidden");
    }

    @Test
    void testExceptionHandling_BadRequest_NoContent() throws Exception {
        StompSession session1 = connect(jwt1);

        CompletableFuture<WebSocketErrorResponse> errorFuture = new CompletableFuture<>();

        session1.subscribe(WebSocketDestinations.USER_QUEUE_PREFIX + WebSocketDestinations.USER_ERROR_QUEUE, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return WebSocketErrorResponse.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                errorFuture.complete((WebSocketErrorResponse) payload);
            }
        });

        Thread.sleep(500);

        // Blank content triggers validation
        SendMessageSocketRequest request = new SendMessageSocketRequest(conversation.getId(), "");
        session1.send(WebSocketDestinations.APP_PREFIX + WebSocketDestinations.CHAT_SEND, request);

        WebSocketErrorResponse error = errorFuture.get(5, TimeUnit.SECONDS);

        assertThat(error).isNotNull();
    }
}
