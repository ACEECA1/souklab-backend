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
public class FormateurGrantDTO {
    @NotBlank(message = "Admin note is required")
    private String adminNote;
}
