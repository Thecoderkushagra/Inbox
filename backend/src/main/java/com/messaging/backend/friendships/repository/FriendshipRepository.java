package com.messaging.backend.friendships.repository;

import com.messaging.backend.friendships.entity.Friendship;
import com.messaging.backend.friendships.enums.FriendshipStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /**
     * Retrieves friendships filtered by status and user, with pagination.
     * Required for searching within accepted friendships (Friend Search foundation).
     */
    @Query("SELECT f FROM Friendship f WHERE f.status = :status AND (f.requester.id = :userId OR f.addressee.id = :userId)")
    Page<Friendship> findFriendshipsByStatusAndUserId(@Param("status") FriendshipStatus status, @Param("userId") UUID userId, Pageable pageable);

    /**
     * Searches accepted friendships where the friend's user details match the keyword.
     * Prevents N+1 and in-memory pagination issues during Friend Search.
     */
    @Query("SELECT f FROM Friendship f " +
           "JOIN f.requester r LEFT JOIN r.profile rp " +
           "JOIN f.addressee a LEFT JOIN a.profile ap " +
           "WHERE f.status = :status AND (r.id = :userId OR a.id = :userId) AND " +
           "((r.id != :userId AND (LOWER(r.username) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(r.email) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(rp.displayName) LIKE LOWER(CONCAT('%', :query, '%')))) OR " +
           "(a.id != :userId AND (LOWER(a.username) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(a.email) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(ap.displayName) LIKE LOWER(CONCAT('%', :query, '%')))))")
    Page<Friendship> searchAcceptedFriendshipsByKeyword(
            @Param("userId") UUID userId, 
            @Param("status") FriendshipStatus status, 
            @Param("query") String query, 
            Pageable pageable);
}
