package com.messaging.backend.media.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.messaging.backend.common.exception.BadRequestException;
import com.messaging.backend.common.exception.ConflictException;
import com.messaging.backend.common.exception.ForbiddenException;
import com.messaging.backend.common.exception.ResourceNotFoundException;
import com.messaging.backend.media.config.MediaProperties;
import com.messaging.backend.media.dto.response.MediaSocketResponse;
import com.messaging.backend.media.entity.MediaAttachment;
import com.messaging.backend.media.enums.MediaType;
import com.messaging.backend.media.mapper.MediaMapper;
import com.messaging.backend.media.repository.MediaAttachmentRepository;
import com.messaging.backend.messaging.entity.Conversation;
import com.messaging.backend.messaging.entity.Message;
import com.messaging.backend.messaging.repository.ConversationRepository;
import com.messaging.backend.messaging.repository.MessageRepository;
import com.messaging.backend.pubsub.constants.PubSubChannels;
import com.messaging.backend.pubsub.dto.RedisEvent;
import com.messaging.backend.pubsub.publisher.RedisEventPublisher;
import com.messaging.backend.websocket.constant.WebSocketDestinations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for media upload and management using Cloudinary storage.
 */
@Slf4j
@Service
public class MediaService {

    private static final String STORAGE_PROVIDER_CLOUDINARY = "CLOUDINARY";

    private final Cloudinary cloudinary;
    private final MediaAttachmentRepository mediaAttachmentRepository;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final MediaProperties mediaProperties;
    private final SimpMessagingTemplate messagingTemplate;
    private final MediaMapper mediaMapper;
    private final RedisEventPublisher redisEventPublisher;

    public MediaService(Cloudinary cloudinary,
                        MediaAttachmentRepository mediaAttachmentRepository,
                        MessageRepository messageRepository,
                        ConversationRepository conversationRepository,
                        MediaProperties mediaProperties,
                        SimpMessagingTemplate messagingTemplate,
                        MediaMapper mediaMapper,
                        RedisEventPublisher redisEventPublisher) {
        this.cloudinary = cloudinary;
        this.mediaAttachmentRepository = mediaAttachmentRepository;
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.mediaProperties = mediaProperties;
        this.messagingTemplate = messagingTemplate;
        this.mediaMapper = mediaMapper;
        this.redisEventPublisher = redisEventPublisher;
    }

    @Transactional
    public MediaAttachment uploadMedia(String requesterId, String messageId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("Cannot upload empty file");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty() || originalFilename.contains("..")) {
            throw new BadRequestException("Invalid or missing filename");
        }

        long fileSize = file.getSize();
        if (fileSize <= 0 || fileSize > mediaProperties.getMaxFileSize()) {
            throw new BadRequestException("File size exceeds maximum allowed 10MB limit");
        }

        String contentType = file.getContentType();
        if (contentType == null || !isContentTypeAllowed(contentType)) {
            throw new BadRequestException("File type is not allowed");
        }

        MediaType mediaType = determineMediaType(contentType);
        if (mediaType == MediaType.OTHER) {
            throw new BadRequestException("Unsupported media type category");
        }

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        if (message.isDeleted()) {
            throw new ConflictException("Cannot attach media to a deleted message");
        }

        String conversationId = message.getConversationId();

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        boolean isParticipant = conversation.getParticipantUserIds().contains(requesterId) ||
                conversation.getParticipants().stream().anyMatch(p -> requesterId.equals(p.getUserId()));

        if (!isParticipant) {
            throw new ForbiddenException("Only conversation participants can upload media");
        }

        String checksum = calculateChecksum(file);

        if (cloudinary.config.cloudName == null || cloudinary.config.cloudName.isBlank()
                || cloudinary.config.cloudName.contains("<")
                || cloudinary.config.apiKey == null || cloudinary.config.apiKey.isBlank()
                || cloudinary.config.apiKey.contains("<")) {
            throw new BadRequestException("Cloudinary is not configured or contains placeholder credentials. Please set a valid CLOUDINARY_URL in your environment.");
        }

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", mediaProperties.getUploadFolder(),
                    "resource_type", "auto"
            ));

            String publicId = (String) uploadResult.get("public_id");
            String secureUrl = (String) uploadResult.get("secure_url");

            MediaAttachment attachment = new MediaAttachment(
                    message.getId(),
                    conversationId,
                    publicId,
                    originalFilename,
                    contentType,
                    fileSize,
                    mediaType,
                    checksum,
                    STORAGE_PROVIDER_CLOUDINARY
            );
            attachment.setCloudinaryUrl(secureUrl);

            MediaAttachment saved = mediaAttachmentRepository.save(attachment);
            broadcastMediaUploaded(saved);
            return saved;
        } catch (Exception e) {
            log.error("Failed to upload file to Cloudinary: {}", e.getMessage(), e);
            throw new BadRequestException("Cloudinary upload failed: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    private void broadcastMediaUploaded(MediaAttachment attachment) {
        try {
            MediaSocketResponse dto = mediaMapper.toSocketResponse(attachment);
            messagingTemplate.convertAndSend(WebSocketDestinations.MEDIA_TOPIC, dto);

            redisEventPublisher.publish(PubSubChannels.MEDIA_CHANNEL,
                    new RedisEvent(null, "MEDIA", null, dto, null));
        } catch (Exception ex) {
            log.error("Failed to broadcast media upload to WebSocket", ex);
        }
    }

    @Transactional(readOnly = true)
    public MediaAttachment getMedia(String requesterId, String storageKey) {
        MediaAttachment attachment = mediaAttachmentRepository.findByStorageKey(storageKey)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found"));

        if (Boolean.TRUE.equals(attachment.getDeleted())) {
            throw new ResourceNotFoundException("Media has been deleted");
        }

        String conversationId = attachment.getConversationId();

        if (conversationId != null) {
            Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
            if (conversation != null && !conversation.getParticipantUserIds().contains(requesterId)) {
                throw new ForbiddenException("Only conversation participants can access this media");
            }
        }

        return attachment;
    }

    @Transactional
    public void softDeleteMedia(String requesterId, String storageKey) {
        MediaAttachment attachment = mediaAttachmentRepository.findByStorageKey(storageKey)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found"));

        if (Boolean.TRUE.equals(attachment.getDeleted())) {
            return;
        }

        attachment.setDeleted(true);
        attachment.setDeletedAt(Instant.now());
        mediaAttachmentRepository.save(attachment);

        try {
            cloudinary.uploader().destroy(storageKey, ObjectUtils.emptyMap());
            log.info("Successfully deleted media from Cloudinary: {}", storageKey);
        } catch (IOException e) {
            log.warn("Failed to delete media from Cloudinary (publicId={}): {}", storageKey, e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<MediaAttachment> getAttachmentsForMessage(String requesterId, String messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        String conversationId = message.getConversationId();

        if (conversationId != null) {
            Conversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
            if (!conversation.getParticipantUserIds().contains(requesterId)) {
                throw new ForbiddenException("Only conversation participants can view attachments");
            }
        }

        return mediaAttachmentRepository.findByMessageId(messageId).stream()
                .filter(a -> !Boolean.TRUE.equals(a.getDeleted()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, List<MediaAttachment>> getAttachmentsForMessages(String requesterId, String conversationId, List<String> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        if (!conversation.getParticipantUserIds().contains(requesterId)) {
            throw new ForbiddenException("Only conversation participants can view attachments");
        }

        List<MediaAttachment> attachments = mediaAttachmentRepository.findByMessageIdInAndDeletedFalse(messageIds);
        return attachments.stream().collect(Collectors.groupingBy(MediaAttachment::getMessageId));
    }

    public String uploadAvatar(String userId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("Cannot upload empty file");
        }

        long fileSize = file.getSize();
        if (fileSize <= 0 || fileSize > mediaProperties.getMaxFileSize()) {
            throw new BadRequestException("File size exceeds maximum allowed 10MB limit");
        }

        String contentType = file.getContentType();
        if (contentType == null || !mediaProperties.getAllowedImageTypes().contains(contentType.toLowerCase())) {
            throw new BadRequestException("Only image files (JPEG, PNG, GIF, WebP) are allowed for avatar");
        }

        if (cloudinary.config.cloudName == null || cloudinary.config.cloudName.isBlank()
                || cloudinary.config.cloudName.contains("<")
                || cloudinary.config.apiKey == null || cloudinary.config.apiKey.isBlank()
                || cloudinary.config.apiKey.contains("<")) {
            throw new BadRequestException("Cloudinary is not configured or contains placeholder credentials. Please set a valid CLOUDINARY_URL in your environment.");
        }

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", mediaProperties.getUploadFolder() + "/avatars",
                    "resource_type", "image"
            ));

            return (String) uploadResult.get("secure_url");
        } catch (Exception e) {
            log.error("Failed to upload avatar to Cloudinary: {}", e.getMessage(), e);
            throw new BadRequestException("Cloudinary avatar upload failed: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    private boolean isContentTypeAllowed(String contentType) {
        String lower = contentType.toLowerCase();
        return mediaProperties.getAllowedImageTypes().contains(lower) ||
               mediaProperties.getAllowedVideoTypes().contains(lower) ||
               mediaProperties.getAllowedAudioTypes().contains(lower) ||
               mediaProperties.getAllowedDocumentTypes().contains(lower) ||
               mediaProperties.getAllowedArchiveTypes().contains(lower);
    }

    private MediaType determineMediaType(String contentType) {
        if (contentType != null) {
            String lower = contentType.toLowerCase();
            if (mediaProperties.getAllowedImageTypes().contains(lower)) return MediaType.IMAGE;
            if (mediaProperties.getAllowedVideoTypes().contains(lower)) return MediaType.VIDEO;
            if (mediaProperties.getAllowedAudioTypes().contains(lower)) return MediaType.AUDIO;
            if (mediaProperties.getAllowedDocumentTypes().contains(lower)) return MediaType.DOCUMENT;
            if (mediaProperties.getAllowedArchiveTypes().contains(lower)) return MediaType.ARCHIVE;
        }
        return MediaType.OTHER;
    }

    private String calculateChecksum(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream is = file.getInputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }
            byte[] hashBytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new IllegalStateException("Failed to calculate checksum", e);
        }
    }
}
