package com.project.souklab.dto.profile;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

/**
 * Partial update (PATCH) request DTO for Artisan profiles.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtisanPatchDTO {

    @Size(max = 5000, message = "Bio must not exceed 5000 characters")
    private String bio;

    @Size(max = 36, message = "Region ID must not exceed 36 characters")
    private String regionId;

    private String region;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @URL(message = "Website must be a valid URL")
    @Size(max = 255, message = "Website must not exceed 255 characters")
    private String website;

    @Size(max = 36, message = "Sub-category ID must not exceed 36 characters")
    private String subCategoryId;

    public String resolveRegionId() {
        if (regionId != null && !regionId.isBlank()) {
            return regionId;
        }
        return region;
    }
}
