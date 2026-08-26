package com.project.souklab.dto.user;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TimeoutRequestDTO {
    @Positive(message = "Timeout duration must be positive")
    private int minutes;
    private String reason;
}
