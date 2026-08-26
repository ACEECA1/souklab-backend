package com.project.souklab.dto.role;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class RoleResponseDTO {
    private Long id;
    private String name;
    private String description;
}
