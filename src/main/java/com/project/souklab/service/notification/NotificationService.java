package com.project.souklab.service.notification;

import com.project.souklab.dao.NotificationRepository;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.dto.notification.NotificationResponseDTO;
import com.project.souklab.exception.ResourceNotFoundException;
import com.project.souklab.exception.UnauthorizedException;
import com.project.souklab.model.Notification;
import com.project.souklab.model.NotificationType;
import com.project.souklab.model.User;
import com.project.souklab.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Creates a new notification for a specific user and sends it over WebSocket.
     * The notification is saved to the database as unread and instantly broadcasted to the user's active session.
     *
     * @param user the recipient of the notification
     * @param message the content of the notification
     * @return a NotificationResponseDTO representing the newly created notification
     */
    @Transactional
    public NotificationResponseDTO createForUser(User user, String message) {
        return createForUser(user, message, null, null);
    }

    /**
     * Creates a new notification for a specific user and sends it over WebSocket.
     * Includes type and targetId for aggregating or identifying specific entities.
     *
     * @param user the recipient of the notification
     * @param message the content of the notification
     * @param type the type of notification
     * @param targetId the UUID string ID of the related entity
     * @return a NotificationResponseDTO representing the newly created notification
     */
    @Transactional
    public NotificationResponseDTO createForUser(User user, String message, NotificationType type, String targetId) {
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setUser(user);
        notification.setType(type);
        notification.setTargetId(targetId);
        notification.setRead(false);

        Notification saved = notificationRepository.save(notification);
        NotificationResponseDTO response = mapToDTO(saved);
        messagingTemplate.convertAndSendToUser(user.getEmail(), "/queue/notifications", response);
        return response;
    }

    /**
     * Creates or updates an aggregated notification.
     *
     * @param user the recipient of the notification
     * @param type the type of notification
     * @param targetId the UUID string ID of the related entity
     * @param baseMessage the base message format
     * @param count the number of occurrences to aggregate
     * @param initiatorUsername the username/email of the latest initiator
     * @return a NotificationResponseDTO representing the newly created or updated notification
     */
    @Transactional
    public NotificationResponseDTO createOrUpdateAggregatedNotification(User user, NotificationType type, String targetId, String baseMessage, int count, String initiatorUsername) {
        Optional<Notification> opt = notificationRepository.findFirstByUserAndTypeAndTargetIdOrderByCreatedAtDesc(user, type, targetId);
        Notification notification;
        String message;
        if (count > 1) {
            message = initiatorUsername + " and " + (count - 1) + " others " + baseMessage;
        } else {
            message = initiatorUsername + " " + baseMessage;
        }

        if (opt.isPresent()) {
            notification = opt.get();
            notification.setMessage(message);
            notification.setRead(false);
            notification.setCreatedAt(LocalDateTime.now());
        } else {
            notification = new Notification();
            notification.setUser(user);
            notification.setType(type);
            notification.setTargetId(targetId);
            notification.setMessage(message);
            notification.setRead(false);
        }

        Notification saved = notificationRepository.save(notification);
        NotificationResponseDTO response = mapToDTO(saved);
        messagingTemplate.convertAndSendToUser(user.getEmail(), "/queue/notifications", response);
        return response;
    }

    /**
     * Sends a system-wide notification to all users who possess the 'ADMIN' role.
     * This is typically used for system alerts, pending approvals, or critical errors.
     *
     * @param message the content of the notification to be sent to admins
     */
    @Transactional
    public void notifyAdmins(String message) {
        userRepository.findAll().stream()
                .filter(user -> user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN")))
                .forEach(admin -> createForUser(admin, message));
    }

    /**
     * Retrieves all notifications belonging to the currently authenticated user,
     * ordered chronologically from the newest to the oldest.
     *
     * @return a list of the user's mapped notifications
     */
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getCurrentUserNotifications() {
        User user = getCurrentUser();
        return notificationRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::mapToDTO)
                .toList();
    }

    /**
     * Marks a specific notification as 'read' if it belongs to the current user.
     *
     * @param notificationId the unique identifier of the notification to mark as read
     * @return the updated NotificationResponseDTO reflecting the read status
     * @throws ResourceNotFoundException if the notification cannot be found or doesn't belong to the user
     */
    @Transactional
    public NotificationResponseDTO markAsRead(String notificationId) {
        User user = getCurrentUser();
        Notification notification = notificationRepository.findByIdAndUser(notificationId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));
        if (!notification.isRead()) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
        return mapToDTO(notification);
    }

    /**
     * Marks all unread notifications belonging to the current authenticated user as 'read'.
     * Useful for a "mark all as read" button in the UI.
     *
     * @throws UnauthorizedException if the current user is not authenticated
     */
    @Transactional
    public void markAllAsRead() {
        User user = getCurrentUser();
        notificationRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .filter(notification -> !notification.isRead())
                .forEach(notification -> notification.setRead(true));
    }

    private User getCurrentUser() {
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) {
            throw new UnauthorizedException("Not authenticated");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private NotificationResponseDTO mapToDTO(Notification notification) {
        return NotificationResponseDTO.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .type(notification.getType())
                .targetId(notification.getTargetId())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
