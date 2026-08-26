package com.project.souklab.dto.notification;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import com.project.souklab.model.NotificationType;

@Value
@Builder
public class NotificationResponseDTO {
    Long id;
    String message;
    boolean isRead;
    NotificationType type;
    Long targetId;
    LocalDateTime createdAt;
}
