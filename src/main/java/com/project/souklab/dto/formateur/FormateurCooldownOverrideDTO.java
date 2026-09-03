package com.project.souklab.dto.formateur;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormateurCooldownOverrideDTO {
    private Boolean canReapply;
    private LocalDateTime cooldownUntil;
}
