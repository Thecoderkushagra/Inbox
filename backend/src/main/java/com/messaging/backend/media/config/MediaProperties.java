package com.messaging.backend.media.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "media")
public class MediaProperties {

    private String uploadFolder = "inbox-media";
    private long maxFileSize = 10485760; // 10MB limit for Cloudinary free tier
    private List<String> allowedImageTypes = List.of("image/jpeg", "image/png", "image/gif", "image/webp");
    private List<String> allowedVideoTypes = List.of("video/mp4", "video/webm");
    private List<String> allowedAudioTypes = List.of("audio/mpeg", "audio/wav", "audio/ogg");
    private List<String> allowedDocumentTypes = List.of("application/pdf", "text/plain", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    private List<String> allowedArchiveTypes = List.of("application/zip", "application/x-tar", "application/gzip");
}
