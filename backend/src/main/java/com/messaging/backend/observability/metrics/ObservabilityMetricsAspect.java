package com.messaging.backend.observability.metrics;

import com.messaging.backend.presence.enums.PresenceStatus;
import com.messaging.backend.presence.repository.UserPresenceRepository;
import com.messaging.backend.ratelimit.service.RateLimitResult;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ObservabilityMetricsAspect {

    private final MeterRegistry meterRegistry;
    private final UserPresenceRepository userPresenceRepository;

    public ObservabilityMetricsAspect(MeterRegistry meterRegistry, UserPresenceRepository userPresenceRepository) {
        this.meterRegistry = meterRegistry;
        this.userPresenceRepository = userPresenceRepository;
    }

    @PostConstruct
    public void registerGauges() {
        Gauge.builder("presence.users.online", userPresenceRepository, 
                      repo -> repo.countByStatus(PresenceStatus.ONLINE))
             .description("Number of online users")
             .register(meterRegistry);
    }

    // Chat Metrics
    @AfterReturning("execution(* com.messaging.backend.messaging.service.MessageService.sendMessage(..))")
    public void onMessageSent() {
        try {
            meterRegistry.counter("chat.messages.sent").increment();
        } catch (Exception e) {}
    }

    @AfterReturning("execution(* com.messaging.backend.readreceipts.service.ReadReceiptService.markDelivered(..))")
    public void onMessageDelivered() {
        try {
            meterRegistry.counter("chat.messages.delivered").increment();
        } catch (Exception e) {}
    }

    @AfterReturning(pointcut = "execution(* com.messaging.backend.readreceipts.service.ReadReceiptService.markSeen(..))", returning = "newlySeen")
    public void onMessageSeen(boolean newlySeen) {
        try {
            if (newlySeen) {
                meterRegistry.counter("chat.messages.read").increment();
            }
        } catch (Exception e) {}
    }

    @AfterReturning(pointcut = "execution(* com.messaging.backend.readreceipts.service.ReadReceiptService.markConversationSeen(..))", returning = "newSeen")
    public void onConversationSeen(int newSeen) {
        try {
            if (newSeen > 0) {
                meterRegistry.counter("chat.messages.read").increment(newSeen);
            }
        } catch (Exception e) {}
    }

    // Notifications Metrics
    @AfterReturning("execution(* com.messaging.backend.notifications.service.NotificationService.createNotification(..))")
    public void onNotificationCreatedAndBroadcast() {
        try {
            meterRegistry.counter("notifications.created").increment();
            meterRegistry.counter("notifications.broadcast").increment(); // create calls broadcast
        } catch (Exception e) {}
    }

    // Media Metrics
    @AfterReturning("execution(* com.messaging.backend.media.service.MediaService.uploadMedia(..))")
    public void onMediaUploaded() {
        try {
            meterRegistry.counter("media.upload.count").increment();
        } catch (Exception e) {}
    }

    @AfterReturning("execution(* com.messaging.backend.media.service.MediaService.getMedia(..))")
    public void onMediaDownloaded() {
        try {
            meterRegistry.counter("media.download.count").increment();
        } catch (Exception e) {}
    }

    // Search Metrics
    @AfterReturning("execution(* com.messaging.backend.search.service.SearchService.search*(..))")
    public void onSearchRequest() {
        try {
            meterRegistry.counter("search.requests").increment();
        } catch (Exception e) {}
    }

    // Rate Limiter Metrics
    @AfterReturning(pointcut = "execution(* com.messaging.backend.ratelimit.service.RateLimitService.consume(..))", returning = "result")
    public void onRateLimitCheck(RateLimitResult result) {
        try {
            if (result != null && !result.allowed()) {
                meterRegistry.counter("ratelimit.requests.blocked").increment();
            }
        } catch (Exception e) {}
    }

    // Redis Pub/Sub Metrics
    @AfterReturning("execution(* com.messaging.backend.pubsub.publisher.RedisEventPublisher.publish(..))")
    public void onPubSubPublished() {
        try {
            meterRegistry.counter("redis.pubsub.events.published").increment();
        } catch (Exception e) {}
    }

    @AfterReturning("execution(* com.messaging.backend.pubsub.listener.RedisEventListener.handleMessage(..))")
    public void onPubSubReceived() {
        try {
            meterRegistry.counter("redis.pubsub.events.received").increment();
        } catch (Exception e) {}
    }
}
