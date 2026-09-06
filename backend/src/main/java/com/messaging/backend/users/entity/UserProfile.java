package com.messaging.backend.users.entity;

import com.messaging.backend.common.entity.BaseDocument;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

/**
 * Represents the public profile of a user.
 *
 * <p>Purpose:
 * Decouples public-facing user details from sensitive authentication credentials (User).
 * Contains non-critical identity information such as display name, bio, and avatars.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "user_profiles")
public class UserProfile extends BaseDocument {

    @Indexed(unique = true)
    private String userId;

    @Size(max = 64)
    @Indexed
    private String displayName;

    @Size(max = 500)
    private String bio;

    @Size(max = 1024)
    private String avatarUrl;

    @Size(max = 1024)
    private String bannerUrl;

    @Size(max = 128)
    private String location;

    @Size(max = 255)
    private String website;

    @Past
    private LocalDate birthDate;

    @Size(max = 32)
    private String gender;

    private boolean verified;

    @Builder
    public UserProfile(String userId, String displayName, String bio, String avatarUrl, 
                       String bannerUrl, String location, String website, LocalDate birthDate, 
                       String gender, boolean verified) {
        this.userId = userId;
        this.displayName = displayName;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
        this.bannerUrl = bannerUrl;
        this.location = location;
        this.website = website;
        this.birthDate = birthDate;
        this.gender = gender;
        this.verified = verified;
    }
}
