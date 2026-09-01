package com.project.souklab.dto.profile;

import com.project.souklab.model.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Profile response DTO scoped to ARTISAN users.
 * Includes artisan-specific fields (teacher, verified, premium, rating, etc.)
 * that are meaningless for client users.
 *
 * NOTE — "validated" (users.is_validated) is intentionally excluded in Phase A.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtisanResponseDTO implements ProfileResponse {

    // --- User fields ---
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String name;
    private String phone;
    /** Placeholder — wired to real upload path in Phase D. */
    private String avatarUrl;
    private AccountStatus accountStatus;
    private Set<String> roles;
    private boolean emailVerified;
    private LocalDateTime emailVerifiedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // --- Artisan fields ---
    private String bio;
    private String regionId;
    private String city;
    private String address;
    private String website;
    private String subCategoryId;

    /**
     * Whether the artisan has been granted Formateur (instructor) status.
     * Only set via the Formateur approve/grant/revoke flow — never by the artisan themselves.
     */
    private boolean teacher;

    /**
     * Whether the artisan's credentials have been admin-verified (artisans.is_verified).
     */
    private boolean verified;

    /**
     * Whether the artisan holds an active premium subscription.
     */
    private boolean premium;

    private double rating;
    private int reviewsCount;
}
