package com.messaging.backend.notifications.controller;

import com.messaging.backend.auth.security.AuthenticatedUser;
import com.messaging.backend.common.dto.response.SuccessResponse;
import com.messaging.backend.notifications.dto.response.NotificationResponse;
import com.messaging.backend.notifications.entity.Notification;
import com.messaging.backend.notifications.mapper.NotificationMapper;
import com.messaging.backend.notifications.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;

    public NotificationController(NotificationService notificationService, NotificationMapper notificationMapper) {
        this.notificationService = notificationService;
        this.notificationMapper = notificationMapper;
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<List<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal AuthenticatedUser currentUser) {

        List<Notification> notifications = notificationService.getNotifications(currentUser.getId());
        List<NotificationResponse> response = notificationMapper.toResponseList(notifications);
        return ResponseEntity.ok(SuccessResponse.success("Notifications retrieved successfully", response));
    }

    @GetMapping("/unread")
    public ResponseEntity<SuccessResponse<List<NotificationResponse>>> getUnreadNotifications(
            @AuthenticationPrincipal AuthenticatedUser currentUser) {

        List<Notification> notifications = notificationService.getUnreadNotifications(currentUser.getId());
        List<NotificationResponse> response = notificationMapper.toResponseList(notifications);
        return ResponseEntity.ok(SuccessResponse.success("Unread notifications retrieved successfully", response));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<SuccessResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal AuthenticatedUser currentUser) {

        long count = notificationService.getUnreadCount(currentUser.getId());
        return ResponseEntity.ok(SuccessResponse.success("Unread count retrieved successfully", count));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<SuccessResponse<NotificationResponse>> markAsRead(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID notificationId) {

        Notification notification = notificationService.markAsRead(notificationId, currentUser.getId());
        NotificationResponse response = notificationMapper.toResponse(notification);
        return ResponseEntity.ok(SuccessResponse.success("Notification marked as read successfully", response));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<SuccessResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal AuthenticatedUser currentUser) {

        notificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok(SuccessResponse.success("All notifications marked as read successfully", null));
    }
}
