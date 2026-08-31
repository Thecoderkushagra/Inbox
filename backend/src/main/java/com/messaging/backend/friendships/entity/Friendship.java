package com.messaging.backend.friendships.entity;

import com.messaging.backend.auth.entity.User;
import com.messaging.backend.common.entity.BaseDocument;
import com.messaging.backend.friendships.enums.FriendshipStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

/**
 * Represents a friend request or friendship relationship between two users in MongoDB.
 */
@Document(collection = "friendships")
@CompoundIndex(name = "requester_addressee_idx", def = "{'requesterId': 1, 'addresseeId': 1}")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Friendship extends BaseDocument {

    @Indexed
    @Field("requesterId")
    private String requesterId;

    @Indexed
    @Field("addresseeId")
    private String addresseeId;

    @Transient
    private User requester;

    @Transient
    private User addressee;

    @Field("status")
    private FriendshipStatus status;

    @Field("respondedAt")
    private Instant respondedAt;

    @Field("blockedAt")
    private Instant blockedAt;
}
