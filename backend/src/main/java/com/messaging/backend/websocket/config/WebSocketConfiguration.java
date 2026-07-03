package com.messaging.backend.websocket.config;

import com.messaging.backend.common.config.CorsProperties;
import com.messaging.backend.ratelimit.filter.WebSocketRateLimitInterceptor;
import com.messaging.backend.websocket.security.JwtChannelInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

/**
 * Foundational WebSocket and STOMP configuration.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    private final CorsProperties corsProperties;
    private final JwtChannelInterceptor jwtChannelInterceptor;
    private final WebSocketRateLimitInterceptor rateLimitInterceptor;

    public WebSocketConfiguration(CorsProperties corsProperties, 
                                  JwtChannelInterceptor jwtChannelInterceptor,
                                  WebSocketRateLimitInterceptor rateLimitInterceptor) {
        this.corsProperties = corsProperties;
        this.jwtChannelInterceptor = jwtChannelInterceptor;
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/user");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        String[] allowedOrigins = corsProperties.getAllowedOrigins().toArray(new String[0]);
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(allowedOrigins)
                .addInterceptors(rateLimitInterceptor)
                .withSockJS()
                .setInterceptors(rateLimitInterceptor);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(jwtChannelInterceptor);
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(128 * 1024);
        registration.setSendTimeLimit(20000);
        registration.setSendBufferSizeLimit(512 * 1024);
    }
}
