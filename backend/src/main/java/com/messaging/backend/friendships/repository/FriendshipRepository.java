package com.messaging.backend.friendships.repository;

import com.messaging.backend.friendships.entity.Friendship;
import com.messaging.backend.friendships.enums.FriendshipStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for managing Friendship entities.
 */
@Repository
public interface FriendshipRepository extends MongoRepository<Friendship, String> {

    boolean existsByRequesterIdAndAddresseeId(String requesterId, String addresseeId);

    Optional<Friendship> findByRequesterIdAndAddresseeId(String requesterId, String addresseeId);

    List<Friendship> findByRequesterId(String requesterId);

    List<Friendship> findByAddresseeId(String addresseeId);

    List<Friendship> findByAddresseeIdAndStatus(String addresseeId, FriendshipStatus status);

    List<Friendship> findByRequesterIdAndStatus(String requesterId, FriendshipStatus status);

    @Query("{'status': ?0, '$or': [{'requesterId': ?1}, {'addresseeId': ?1}]}")
    List<Friendship> findFriendshipsByStatusAndUserId(FriendshipStatus status, String userId);

    @Query("{'status': ?0, '$or': [{'requesterId': ?1}, {'addresseeId': ?1}]}")
    Page<Friendship> findFriendshipsByStatusAndUserId(FriendshipStatus status, String userId, Pageable pageable);
}
