package com.project.souklab.dto.formateur;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormateurRevokeDTO {
    @NotBlank(message = "Reason is required")
    private String reason;
}
