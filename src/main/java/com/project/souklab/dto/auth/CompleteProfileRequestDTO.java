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
    // NOTE: isTeacher is intentionally absent — it is controlled exclusively by the
    // Formateur request/approve/grant/revoke flow. Any "isTeacher" key in the request
    // body is silently ignored by Jackson (default unknown-field behavior).
    private String bio;
    private String address;
    private String website;
    private String subCategoryId;
    private List<String> materialIds;
    private List<String> epoqueIds;
    private List<String> techniqueIds;

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
