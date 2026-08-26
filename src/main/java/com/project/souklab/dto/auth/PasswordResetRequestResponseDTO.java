package com.project.souklab.dto.auth;

import lombok.Builder;
import lombok.Data;
import com.project.souklab.model.PasswordResetRequest;

import java.time.LocalDateTime;

@Data
@Builder
public class PasswordResetRequestResponseDTO {
    private Long id;
    private String username;
    private PasswordResetRequest.ResetStatus status;
    private LocalDateTime createdAt;
}
