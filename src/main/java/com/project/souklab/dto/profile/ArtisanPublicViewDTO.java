package com.project.souklab.dto.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtisanPublicViewDTO {

    private String id;
    private String bio;
    private String city;
    private String regionId;
    private String subCategoryId;
    private double rating;
    private int reviewsCount;
    private boolean teacher;
    private boolean verified;
    private String avatarUrl;
    private LocalDateTime createdAt;

    // Contact info gating
    private boolean contactInfoLocked;
    private String name;
    private String phone;
    private String email;
    private String website;
    private String address;
}
