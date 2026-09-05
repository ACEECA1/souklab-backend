package com.project.souklab.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data transfer object representing an avatar entry in a user's gallery.
 * Exposes application file-serving URLs for the three processed resolution tiers
 * (original, medium, thumbnail) along with verification metadata and active status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvatarResponseDTO {

    /**
     * Unique identifier of the avatar record.
     */
    private String id;

    /**
     * Application endpoint URL for retrieving the original resolution variant.
     */
    private String urlOriginal;

    /**
     * Application endpoint URL for retrieving the medium resolution variant.
     */
    private String urlMedium;

    /**
     * Application endpoint URL for retrieving the thumbnail resolution variant.
     */
    private String urlThumbnail;

    /**
     * Original client filename prior to sanitization.
     */
    private String originalFilename;

    /**
     * Verified MIME content type of the uploaded image.
     */
    private String contentType;

    /**
     * File size in bytes of the original uploaded image.
     */
    private long fileSize;

    /**
     * Indicates whether this avatar is currently selected as the active profile avatar.
     */
    @JsonProperty("isActive")
    private boolean isActive;

    /**
     * Timestamp when the avatar was processed and persisted.
     */
    private LocalDateTime uploadedAt;
}
