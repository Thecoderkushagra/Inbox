package com.messaging.backend.friendships.repository;

import com.messaging.backend.friendships.entity.Friendship;
import com.messaging.backend.friendships.enums.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing Friendship entities.
 */
@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {

    boolean existsByRequesterIdAndAddresseeId(UUID requesterId, UUID addresseeId);

    Optional<Friendship> findByRequesterIdAndAddresseeId(UUID requesterId, UUID addresseeId);

    List<Friendship> findByRequesterId(UUID requesterId);

    List<Friendship> findByAddresseeId(UUID addresseeId);

    List<Friendship> findByAddresseeIdAndStatus(UUID addresseeId, FriendshipStatus status);

    List<Friendship> findByRequesterIdAndStatus(UUID requesterId, FriendshipStatus status);

    @Query("SELECT f FROM Friendship f WHERE f.status = :status AND (f.requester.id = :userId OR f.addressee.id = :userId)")
    List<Friendship> findFriendshipsByStatusAndUserId(@Param("status") FriendshipStatus status, @Param("userId") UUID userId);

}
