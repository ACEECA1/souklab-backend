package com.project.souklab.dto.profile;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Partial update (PATCH) request DTO for Client profiles.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientPatchDTO {

    @Size(max = 5000, message = "Bio must not exceed 5000 characters")
    private String bio;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @Size(max = 36, message = "Region ID must not exceed 36 characters")
    private String regionId;

    private String region;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 255, message = "Company name must not exceed 255 characters")
    private String companyName;

    @Size(max = 50, message = "Client type must not exceed 50 characters")
    private String clientType;

    public String resolveRegionId() {
        if (regionId != null && !regionId.isBlank()) {
            return regionId;
        }
        return region;
    }
}
