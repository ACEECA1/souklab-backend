package com.project.souklab.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordRequestDTO {
    @NotBlank
    private String oldPassword;
    @NotBlank
    private String newPassword;
}
