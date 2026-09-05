package com.project.souklab.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity representing an avatar uploaded by a user with references to its 3 processed resolution variants.
 * Supports gallery history of up to 10 avatars per user with exactly one active avatar at a time.
 * Hard-deleted upon removal to purge associated binary storage objects.
 */
@Entity
@Table(
    name = "user_avatars",
    indexes = {
        @Index(name = "idx_user_avatars_user_id", columnList = "user_id"),
        @Index(name = "idx_user_avatars_user_active", columnList = "user_id, is_active"),
        @Index(name = "idx_user_avatars_user_uploaded", columnList = "user_id, uploaded_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAvatar extends BaseEntity {

    /**
     * The owning user who uploaded this avatar.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Unique storage key for the original resolution variant (capped at 2000px).
     */
    @Column(name = "storage_key_original", nullable = false, length = 255)
    private String storageKeyOriginal;

    /**
     * Unique storage key for the medium resolution variant (capped at 500px).
     */
    @Column(name = "storage_key_medium", nullable = false, length = 255)
    private String storageKeyMedium;

    /**
     * Unique storage key for the thumbnail resolution variant (capped at 150px).
     */
    @Column(name = "storage_key_thumbnail", nullable = false, length = 255)
    private String storageKeyThumbnail;

    /**
     * Original client filename prior to storage key generation.
     */
    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    /**
     * Verified MIME content type of the uploaded image (e.g. image/jpeg, image/png).
     */
    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType;

    /**
     * Total size in bytes of the original uploaded image file.
     */
    @Column(name = "file_size", nullable = false)
    private long fileSize;

    /**
     * Flag indicating whether this avatar is currently selected as the active profile avatar.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = false;

    /**
     * Timestamp when this avatar was processed and persisted.
     */
    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;
}
