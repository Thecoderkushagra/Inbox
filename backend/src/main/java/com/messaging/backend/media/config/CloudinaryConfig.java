package com.messaging.backend.media.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cloudinary client configuration for media management.
 */
@Configuration
public class CloudinaryConfig {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryConfig.class);
    private static final Pattern CLOUDINARY_URL_PATTERN = Pattern.compile("^cloudinary://([^:]+):([^@]+)@(.*)$");

    @Bean
    public Cloudinary cloudinary(
            @Value("${cloudinary.url:}") String cloudinaryUrl,
            @Value("${cloudinary.cloud-name:}") String cloudName,
            @Value("${cloudinary.api-key:}") String apiKey,
            @Value("${cloudinary.api-secret:}") String apiSecret) {

        String cleanUrl = clean(cloudinaryUrl);
        if (cleanUrl.startsWith("CLOUDINARY_URL=")) {
            cleanUrl = cleanUrl.substring("CLOUDINARY_URL=".length()).trim();
        }

        String cleanCloudName = clean(cloudName);
        String cleanApiKey = clean(apiKey);
        String cleanApiSecret = clean(apiSecret);

        if (!cleanCloudName.isBlank() && !cleanApiKey.isBlank() && !cleanApiSecret.isBlank()) {
            log.info("Configuring Cloudinary using discrete credentials for cloud: {}", cleanCloudName);
            return new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cleanCloudName,
                    "api_key", cleanApiKey,
                    "api_secret", cleanApiSecret,
                    "secure", true
            ));
        }

        if (!cleanUrl.isBlank()) {
            Matcher matcher = CLOUDINARY_URL_PATTERN.matcher(cleanUrl);
            if (matcher.matches()) {
                String parsedKey = clean(matcher.group(1));
                String parsedSecret = clean(matcher.group(2));
                String parsedCloud = clean(matcher.group(3));

                log.info("Configuring Cloudinary using parsed URL credentials for cloud: {}", parsedCloud);
                return new Cloudinary(ObjectUtils.asMap(
                        "cloud_name", parsedCloud,
                        "api_key", parsedKey,
                        "api_secret", parsedSecret,
                        "secure", true
                ));
            }

            log.info("Configuring Cloudinary using raw connection string");
            return new Cloudinary(cleanUrl);
        }

        log.warn("No Cloudinary credentials provided; media uploads will fail until configured.");
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cleanCloudName,
                "api_key", cleanApiKey,
                "api_secret", cleanApiSecret,
                "secure", true
        ));
    }

    private static String clean(String s) {
        if (s == null) {
            return "";
        }
        s = s.trim();
        if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))) {
            if (s.length() >= 2) {
                s = s.substring(1, s.length() - 1).trim();
            }
        }
        return s;
    }
}
