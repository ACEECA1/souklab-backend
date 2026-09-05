package com.project.souklab.dao;

import com.project.souklab.model.UserAvatar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Data access operations for {@link UserAvatar} entities.
 * Provides gallery listing, quota checking, active avatar resolution, and ownership-scoped lookups.
 */
@Repository
public interface UserAvatarRepository extends JpaRepository<UserAvatar, String> {

    /**
     * Retrieves all avatar entries belonging to a given user, sorted from newest to oldest.
     *
     * @param userId the unique identifier of the user
     * @return ordered list of user avatars
     */
    List<UserAvatar> findByUserIdOrderByUploadedAtDesc(String userId);

    /**
     * Counts the total number of avatars currently stored for a given user.
     *
     * @param userId the unique identifier of the user
     * @return total avatar count for the user
     */
    long countByUserId(String userId);

    /**
     * Finds a specific avatar by its unique identifier and owning user identifier to enforce strict ownership isolation.
     *
     * @param id the unique identifier of the avatar
     * @param userId the unique identifier of the user
     * @return optional containing the avatar if found and owned by the user
     */
    Optional<UserAvatar> findByIdAndUserId(String id, String userId);

    /**
     * Finds the single active avatar currently selected for the given user.
     *
     * @param userId the unique identifier of the user
     * @return optional containing the active avatar if one is currently active
     */
    Optional<UserAvatar> findByUserIdAndIsActiveTrue(String userId);
}
