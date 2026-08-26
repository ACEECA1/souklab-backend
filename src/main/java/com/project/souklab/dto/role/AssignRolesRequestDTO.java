package com.project.souklab.dto.role;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class AssignRolesRequestDTO {
    private String userId;

    @NotEmpty(message = "At least one role is required")
    private Set<String> roleNames;
}
