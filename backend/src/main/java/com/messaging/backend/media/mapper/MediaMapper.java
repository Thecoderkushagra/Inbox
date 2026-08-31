package com.messaging.backend.media.mapper;

import com.messaging.backend.media.dto.response.MediaAttachmentResponse;
import com.messaging.backend.media.dto.response.MediaSocketResponse;
import com.messaging.backend.media.entity.MediaAttachment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MediaMapper {

    public MediaAttachmentResponse toResponse(MediaAttachment attachment) {
        if (attachment == null) {
            return null;
        }

        return new MediaAttachmentResponse(
                attachment.getId(),
                attachment.getStorageKey(),
                attachment.getCloudinaryUrl(),
                attachment.getOriginalFilename(),
                attachment.getContentType(),
                attachment.getMediaType() != null ? attachment.getMediaType().name() : null,
                attachment.getFileSize(),
                attachment.getChecksum(),
                attachment.getCreatedAt()
        );
    }

    public MediaSocketResponse toSocketResponse(MediaAttachment attachment) {
        if (attachment == null) {
            return null;
        }

        return new MediaSocketResponse(
                attachment.getId(),
                attachment.getMessageId(),
                attachment.getConversationId(),
                attachment.getStorageKey(),
                attachment.getCloudinaryUrl(),
                attachment.getOriginalFilename(),
                attachment.getContentType(),
                attachment.getMediaType() != null ? attachment.getMediaType().name() : null,
                attachment.getFileSize(),
                attachment.getCreatedAt()
        );
    }

    public List<MediaAttachmentResponse> toResponseList(List<MediaAttachment> attachments) {
        if (attachments == null) {
            return List.of();
        }

        List<MediaAttachmentResponse> responses = new ArrayList<>();
        for (MediaAttachment attachment : attachments) {
            responses.add(toResponse(attachment));
        }
        return responses;
    }
}
