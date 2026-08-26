package com.project.souklab.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteProfileRequestDTO {

    // Common fields
    private String regionId;
    private String region;
    private String city;

    // Artisan-specific fields
    private String bio;
    private String address;
    private String website;
    private String subCategoryId;
    private List<String> materialIds;
    private List<String> epoqueIds;
    private List<String> techniqueIds;
    private Boolean isTeacher;

    // Client-specific fields
    private String clientType;
    private String companyName;

    public String resolveRegionId() {
        if (regionId != null && !regionId.isBlank()) {
            return regionId;
        }
        return region;
    }
}
