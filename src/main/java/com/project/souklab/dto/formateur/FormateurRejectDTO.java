package com.project.souklab.dto.formateur;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormateurRejectDTO {
    @NotBlank(message = "Admin note is required")
    private String adminNote;

    private LocalDateTime cooldownUntil;

    private Boolean canReapply;
}
