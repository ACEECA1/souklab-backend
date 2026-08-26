package com.project.souklab.dto.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BulkAssignRoleRequestDTO {
    @NotBlank(message = "Role name is required")
    private String roleName;

    @NotEmpty(message = "User IDs are required")
    private List<String> userIds;
}
