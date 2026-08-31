package com.messaging.backend.media.controller;

import com.messaging.backend.auth.security.AuthenticatedUser;
import com.messaging.backend.common.dto.response.SuccessResponse;
import com.messaging.backend.media.dto.response.MediaAttachmentResponse;
import com.messaging.backend.media.entity.MediaAttachment;
import com.messaging.backend.media.mapper.MediaMapper;
import com.messaging.backend.media.service.MediaService;
import com.messaging.backend.ratelimit.annotation.RateLimited;
import com.messaging.backend.ratelimit.constants.RateLimitPolicy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/media")
public class MediaController {

    private final MediaService mediaService;
    private final MediaMapper mediaMapper;

    public MediaController(MediaService mediaService, MediaMapper mediaMapper) {
        this.mediaService = mediaService;
        this.mediaMapper = mediaMapper;
    }

    @PostMapping("/messages/{messageId}")
    @RateLimited(policy = RateLimitPolicy.MEDIA_UPLOAD)
    public ResponseEntity<SuccessResponse<MediaAttachmentResponse>> uploadAttachment(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable String messageId,
            @RequestParam("file") MultipartFile file) {

        MediaAttachment attachment = mediaService.uploadMedia(currentUser.getId(), messageId, file);
        MediaAttachmentResponse response = mediaMapper.toResponse(attachment);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.success("Media uploaded successfully", response));
    }

    @GetMapping("/{storageKey}")
    public ResponseEntity<?> getAttachment(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable String storageKey) {

        MediaAttachment attachment = mediaService.getMedia(currentUser.getId(), storageKey);
        if (attachment.getCloudinaryUrl() != null && !attachment.getCloudinaryUrl().isBlank()) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(attachment.getCloudinaryUrl()))
                    .build();
        }
        MediaAttachmentResponse response = mediaMapper.toResponse(attachment);
        return ResponseEntity.ok(SuccessResponse.success(response));
    }

    @GetMapping("/messages/{messageId}")
    public ResponseEntity<SuccessResponse<List<MediaAttachmentResponse>>> listAttachments(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable String messageId) {

        List<MediaAttachment> attachments = mediaService.getAttachmentsForMessage(currentUser.getId(), messageId);
        List<MediaAttachmentResponse> response = mediaMapper.toResponseList(attachments);
        return ResponseEntity.ok(SuccessResponse.success("Attachments retrieved successfully", response));
    }

    @DeleteMapping("/{storageKey}")
    public ResponseEntity<Void> deleteAttachment(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable String storageKey) {

        mediaService.softDeleteMedia(currentUser.getId(), storageKey);
        return ResponseEntity.noContent().build();
    }
}
