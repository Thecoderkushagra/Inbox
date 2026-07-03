package com.messaging.backend.media.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "media")
public class MediaProperties {

    private String storagePath = "./media-storage";
    private long maxFileSize = 52428800; // 50MB
    private List<String> allowedImageTypes = List.of("image/jpeg", "image/png", "image/gif", "image/webp");
    private List<String> allowedVideoTypes = List.of("video/mp4", "video/webm");
    private List<String> allowedAudioTypes = List.of("audio/mpeg", "audio/wav", "audio/ogg");
    private List<String> allowedDocumentTypes = List.of("application/pdf", "text/plain", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    private List<String> allowedArchiveTypes = List.of("application/zip", "application/x-tar", "application/gzip");

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public List<String> getAllowedImageTypes() {
        return allowedImageTypes;
    }

    public void setAllowedImageTypes(List<String> allowedImageTypes) {
        this.allowedImageTypes = allowedImageTypes;
    }

    public List<String> getAllowedVideoTypes() {
        return allowedVideoTypes;
    }

    public void setAllowedVideoTypes(List<String> allowedVideoTypes) {
        this.allowedVideoTypes = allowedVideoTypes;
    }

    public List<String> getAllowedAudioTypes() {
        return allowedAudioTypes;
    }

    public void setAllowedAudioTypes(List<String> allowedAudioTypes) {
        this.allowedAudioTypes = allowedAudioTypes;
    }

    public List<String> getAllowedDocumentTypes() {
        return allowedDocumentTypes;
    }

    public void setAllowedDocumentTypes(List<String> allowedDocumentTypes) {
        this.allowedDocumentTypes = allowedDocumentTypes;
    }

    public List<String> getAllowedArchiveTypes() {
        return allowedArchiveTypes;
    }

    public void setAllowedArchiveTypes(List<String> allowedArchiveTypes) {
        this.allowedArchiveTypes = allowedArchiveTypes;
    }
}
