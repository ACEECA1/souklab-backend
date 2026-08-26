package com.project.souklab.dto.admin;

import lombok.Data;
import java.time.LocalDateTime;
import com.project.souklab.model.AuditLogAction;

@Data
public class AuditLogDTO {
    private Long id;
    private AuditLogAction action;
    private String details;
    private String username;
    private LocalDateTime createdAt;
}
