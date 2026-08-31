package com.messaging.backend.media.entity;

import com.messaging.backend.common.entity.BaseDocument;
import com.messaging.backend.media.enums.MediaType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Represents a media attachment stored in Cloudinary and recorded in MongoDB.
 */
@Getter
@Setter
@Document(collection = "media_attachments")
@CompoundIndex(name = "idx_media_msg_deleted", def = "{'messageId': 1, 'deleted': 1}")
public class MediaAttachment extends BaseDocument {

    @Indexed
    private String messageId;

    @Indexed
    private String conversationId;

    @Indexed(unique = true)
    private String storageKey;

    private String cloudinaryPublicId;

    private String cloudinaryUrl;

    private String originalFilename;

    private String contentType;

    private Long fileSize;

    private MediaType mediaType;

    private String checksum;

    private String storageProvider = "CLOUDINARY";

    private Boolean deleted = false;

    private Instant deletedAt;

    public MediaAttachment() {
    }

    public MediaAttachment(String messageId, String conversationId, String storageKey, String originalFilename, String contentType,
                           Long fileSize, MediaType mediaType, String checksum, String storageProvider) {
        this.messageId = messageId;
        this.conversationId = conversationId;
        this.storageKey = storageKey;
        this.cloudinaryPublicId = storageKey;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.mediaType = mediaType;
        this.checksum = checksum;
        this.storageProvider = storageProvider != null ? storageProvider : "CLOUDINARY";
        this.deleted = false;
    }
}
