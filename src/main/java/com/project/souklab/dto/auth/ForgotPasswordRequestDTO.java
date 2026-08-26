package com.project.souklab.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequestDTO {
    @NotBlank
    private String username;
}
