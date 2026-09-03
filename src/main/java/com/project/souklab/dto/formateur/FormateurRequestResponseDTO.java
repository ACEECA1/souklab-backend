package com.project.souklab.dto.formateur;

import com.project.souklab.model.FormateurRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormateurRequestResponseDTO {
    private String id;
    private String artisanId;
    private String artisanName;
    private String artisanEmail;
    private FormateurRequestStatus status;
    private String motivation;
    private String adminNote;
    private boolean canReapply;
    private LocalDateTime cooldownUntil;
    private String decidedByAdminId;
    private String decidedByAdminEmail;
    private LocalDateTime decidedAt;
    private LocalDateTime createdAt;
}
