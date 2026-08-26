package com.project.souklab.dto.role;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleResponseDTO {
    private String id;
    private String name;
    private String description;
}
