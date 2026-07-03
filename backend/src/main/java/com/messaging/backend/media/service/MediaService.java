package com.messaging.backend.media.service;

import com.messaging.backend.common.exception.BadRequestException;
import com.messaging.backend.common.exception.ConflictException;
import com.messaging.backend.common.exception.ForbiddenException;
import com.messaging.backend.common.exception.ResourceNotFoundException;
import com.messaging.backend.media.entity.MediaAttachment;
import com.messaging.backend.media.enums.MediaType;
import com.messaging.backend.media.repository.MediaAttachmentRepository;
import com.messaging.backend.messaging.entity.Message;
import com.messaging.backend.messaging.repository.ConversationParticipantRepository;
import com.messaging.backend.messaging.repository.MessageRepository;
import com.messaging.backend.websocket.constant.WebSocketDestinations;
import com.messaging.backend.media.config.MediaProperties;
import com.messaging.backend.media.mapper.MediaMapper;
import com.messaging.backend.media.dto.response.MediaSocketResponse;
import com.messaging.backend.pubsub.publisher.RedisEventPublisher;
import com.messaging.backend.pubsub.constants.PubSubChannels;
import com.messaging.backend.pubsub.dto.RedisEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MediaService {

    private static final Logger log = LoggerFactory.getLogger(MediaService.class);
    private static final String STORAGE_PROVIDER_LOCAL = "LOCAL";

    private final MediaAttachmentRepository mediaAttachmentRepository;
    private final MessageRepository messageRepository;
    private final ConversationParticipantRepository conversationParticipantRepository;
    private final Path storageRoot;
    private final SimpMessagingTemplate messagingTemplate;
    private final MediaMapper mediaMapper;
    private final MediaProperties mediaProperties;
    private final RedisEventPublisher redisEventPublisher;

    public MediaService(MediaAttachmentRepository mediaAttachmentRepository,
                        MessageRepository messageRepository,
                        ConversationParticipantRepository conversationParticipantRepository,
                        MediaProperties mediaProperties,
                        SimpMessagingTemplate messagingTemplate,
                        MediaMapper mediaMapper,
                        RedisEventPublisher redisEventPublisher) {
        this.mediaAttachmentRepository = mediaAttachmentRepository;
        this.messageRepository = messageRepository;
        this.conversationParticipantRepository = conversationParticipantRepository;
        this.mediaProperties = mediaProperties;
        this.storageRoot = Paths.get(mediaProperties.getStoragePath()).toAbsolutePath().normalize();
        this.messagingTemplate = messagingTemplate;
        this.mediaMapper = mediaMapper;
        this.redisEventPublisher = redisEventPublisher;
        
        try {
            Files.createDirectories(this.storageRoot);
        } catch (IOException e) {
            throw new IllegalStateException("Could not initialize storage directory", e);
        }
    }

    public record MediaDownload(MediaAttachment metadata, Path filePath) {}

    @Transactional
    public MediaAttachment uploadMedia(UUID requesterId, UUID messageId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("Cannot upload empty file");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty() || originalFilename.contains("..")) {
            throw new BadRequestException("Invalid or missing filename");
        }

        long fileSize = file.getSize();
        if (fileSize <= 0 || fileSize > mediaProperties.getMaxFileSize()) {
            throw new BadRequestException("File size is invalid or exceeds the maximum allowed size");
        }

        String contentType = file.getContentType();
        if (contentType == null || !isContentTypeAllowed(contentType)) {
            throw new BadRequestException("File type is not allowed");
        }

        MediaType mediaType = determineMediaType(contentType);
        if (mediaType == MediaType.OTHER) {
            throw new BadRequestException("Unsupported media type category");
        }

        // Validate extension matches MIME type when possible
        String fileExt = getFileExtension(originalFilename);
        if (fileExt != null) {
            MediaType extType = determineMediaTypeFromExtension(fileExt);
            if (extType != MediaType.OTHER && extType != mediaType) {
                log.warn("MIME type {} does not match file extension .{}", contentType, fileExt);
                throw new BadRequestException("File extension does not match content type");
            }
        }

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        if (message.isDeleted()) {
            throw new ConflictException("Cannot attach media to a deleted message");
        }

        UUID conversationId = message.getConversation().getId();
        boolean isParticipant = conversationParticipantRepository.existsByConversationIdAndUserId(conversationId, requesterId);
        if (!isParticipant) {
            throw new ForbiddenException("Only conversation participants can upload media");
        }

        UUID storageKey = UUID.randomUUID();
        String checksum = calculateChecksum(file);

        MediaAttachment attachment = new MediaAttachment(
                message,
                storageKey,
                originalFilename,
                contentType,
                fileSize,
                mediaType,
                checksum,
                STORAGE_PROVIDER_LOCAL
        );

        attachment = mediaAttachmentRepository.save(attachment);

        try {
            Path targetLocation = this.storageRoot.resolve(storageKey.toString()).normalize();
            if (!targetLocation.startsWith(this.storageRoot)) {
                throw new IllegalStateException("Cannot store file outside current directory");
            }
            if (Files.exists(targetLocation)) {
                throw new IllegalStateException("Storage key collision");
            }
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("Successfully uploaded media file with storage key: {}", storageKey);
        } catch (IOException ex) {
            log.error("Failed to store file physically", ex);
            // This exception bubbles up and rolls back the database transaction automatically
            throw new IllegalStateException("Could not store file", ex);
        }

        broadcastMediaUploaded(attachment);

        return attachment;
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
    public MediaDownload getMedia(UUID requesterId, UUID storageKey) {
        MediaAttachment attachment = mediaAttachmentRepository.findByStorageKey(storageKey)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found"));

        if (Boolean.TRUE.equals(attachment.getDeleted())) {
            throw new ResourceNotFoundException("Media not found or has been deleted");
        }

        UUID conversationId = attachment.getMessage().getConversation().getId();
        if (!conversationParticipantRepository.existsByConversationIdAndUserId(conversationId, requesterId)) {
            throw new ForbiddenException("Only conversation participants can access this media");
        }

        Path filePath = this.storageRoot.resolve(storageKey.toString()).normalize();
        if (!Files.exists(filePath)) {
            log.warn("Physical file missing for storage key: {}", storageKey);
            throw new ResourceNotFoundException("Physical file not found");
        }

        return new MediaDownload(attachment, filePath);
    }

    @Transactional
    public void softDeleteMedia(UUID requesterId, UUID storageKey) {
        MediaAttachment attachment = mediaAttachmentRepository.findByStorageKey(storageKey)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found"));

        if (Boolean.TRUE.equals(attachment.getDeleted())) {
            return;
        }

        // Must be participant to delete? Or only sender? The rules say "only conversation participants can access". 
        // Typically only the sender or an admin can delete, but let's just ensure they are a participant for now, 
        // or ensure they are the sender of the message. 
        // The rule says: "only conversation participants can access attachments"
        UUID conversationId = attachment.getMessage().getConversation().getId();
        if (!conversationParticipantRepository.existsByConversationIdAndUserId(conversationId, requesterId)) {
            throw new ForbiddenException("Must be a conversation participant");
        }

        attachment.setDeleted(true);
        attachment.setDeletedAt(Instant.now());
        mediaAttachmentRepository.save(attachment);
        log.info("Soft deleted media with storage key: {}", storageKey);
    }

    @Transactional(readOnly = true)
    public List<MediaAttachment> getAttachmentsForMessage(UUID requesterId, UUID messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        UUID conversationId = message.getConversation().getId();
        if (!conversationParticipantRepository.existsByConversationIdAndUserId(conversationId, requesterId)) {
            throw new ForbiddenException("Only conversation participants can view attachments");
        }

        return mediaAttachmentRepository.findByMessageId(messageId).stream()
                .filter(a -> !Boolean.TRUE.equals(a.getDeleted()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<UUID, List<MediaAttachment>> getAttachmentsForMessages(UUID requesterId, UUID conversationId, List<UUID> messageIds) {
        if (!conversationParticipantRepository.existsByConversationIdAndUserId(conversationId, requesterId)) {
            throw new ForbiddenException("Only conversation participants can view attachments");
        }
        
        if (messageIds == null || messageIds.isEmpty()) {
            return Map.of();
        }

        List<MediaAttachment> attachments = mediaAttachmentRepository.findByMessageIdInAndDeletedFalse(messageIds);
        return attachments.stream().collect(Collectors.groupingBy(a -> a.getMessage().getId()));
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

    private MediaType determineMediaTypeFromExtension(String extension) {
        String lower = extension.toLowerCase();
        if (lower.equals("jpg") || lower.equals("jpeg") || lower.equals("png") || lower.equals("gif") || lower.equals("webp")) return MediaType.IMAGE;
        if (lower.equals("mp4") || lower.equals("webm") || lower.equals("avi") || lower.equals("mov")) return MediaType.VIDEO;
        if (lower.equals("mp3") || lower.equals("wav") || lower.equals("ogg")) return MediaType.AUDIO;
        if (lower.equals("pdf") || lower.equals("doc") || lower.equals("docx") || lower.equals("txt")) return MediaType.DOCUMENT;
        if (lower.equals("zip") || lower.equals("tar") || lower.equals("gz") || lower.equals("rar")) return MediaType.ARCHIVE;
        return MediaType.OTHER;
    }

    private String getFileExtension(String filename) {
        if (filename == null) return null;
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1);
        }
        return null;
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
