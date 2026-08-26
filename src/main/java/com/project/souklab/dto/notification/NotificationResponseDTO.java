package com.project.souklab.dto.notification;

import com.project.souklab.model.NotificationType;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class NotificationResponseDTO {
    String id;
    String message;
    boolean isRead;
    NotificationType type;
    String targetId;
    LocalDateTime createdAt;
}
