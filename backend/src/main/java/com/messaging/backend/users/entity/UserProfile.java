package com.messaging.backend.users.entity;

import com.messaging.backend.auth.entity.User;
import com.messaging.backend.common.entity.BaseEntity;
import com.messaging.backend.users.enums.ProfileVisibility;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Represents the public profile of a user.
 *
 * <p>Purpose:
 * Decouples public-facing user details from sensitive authentication credentials (User).
 * Contains non-critical identity information such as display name, bio, and avatars.
 *
 * <p>Relationships:
 * Holds a One-to-One association with the authentication User entity. The user_id is the foreign key.
 *
 * <p>Validation:
 * String lengths are constrained at both the Java Bean Validation layer and JPA schema layer
 * to prevent database errors and enforce reasonable limits on user inputs.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "user_profiles")
public class UserProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Size(max = 64)
    @Column(name = "display_name", length = 64)
    private String displayName;

    @Size(max = 500)
    @Column(length = 500)
    private String bio;

    @Size(max = 1024)
    @Column(name = "avatar_url", length = 1024)
    private String avatarUrl;

    @Size(max = 1024)
    @Column(name = "banner_url", length = 1024)
    private String bannerUrl;

    @Size(max = 128)
    @Column(length = 128)
    private String location;

    @Size(max = 255)
    @Column(length = 255)
    private String website;

    @Past
    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Size(max = 32)
    @Column(length = 32)
    private String gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_visibility", nullable = false, length = 32)
    private ProfileVisibility profileVisibility;

    @Column(nullable = false)
    private boolean verified;

    @Builder
    public UserProfile(User user, String displayName, String bio, String avatarUrl, 
                       String bannerUrl, String location, String website, LocalDate birthDate, 
                       String gender, ProfileVisibility profileVisibility, boolean verified) {
        this.user = user;
        this.displayName = displayName;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
        this.bannerUrl = bannerUrl;
        this.location = location;
        this.website = website;
        this.birthDate = birthDate;
        this.gender = gender;
        this.profileVisibility = profileVisibility != null ? profileVisibility : ProfileVisibility.PUBLIC;
        this.verified = verified;
    }
}
