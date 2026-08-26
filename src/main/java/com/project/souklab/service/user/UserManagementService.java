package com.project.souklab.service.user;

import lombok.RequiredArgsConstructor;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.dto.auth.UserResponseDTO;
import com.project.souklab.dto.common.PaginatedResponse;
import com.project.souklab.model.Role;
import com.project.souklab.model.User;
import com.project.souklab.service.audit.AuditLogService;
import com.project.souklab.service.security.RefreshTokenService;
import com.project.souklab.util.AppException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;
import java.util.List;
import java.util.HashSet;
import com.project.souklab.model.NotificationType;
import com.project.souklab.model.AuditLogAction;
import java.time.LocalDateTime;
import com.project.souklab.service.notification.NotificationService;
import com.project.souklab.dao.RoleRepository;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuditLogService auditLogService;
    private final RefreshTokenService refreshTokenService;
    private final NotificationService notificationService;

    /**
     * Retrieves a paginated list of all users in the system.
     *
     * @param pageable the pagination parameters specifying page size, number, and sorting
     * @return a paginated response containing a list of all users as UserResponseDTOs
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<UserResponseDTO> getAllUsers(String search, Pageable pageable) {
        Page<User> page;
        if (search != null && !search.isBlank()) {
            page = userRepository.searchUsers(search, pageable);
        } else {
            page = userRepository.findAll(pageable);
        }
        return PaginatedResponse.from(page.map(this::mapToDTO));
    }

    /**
     * Assigns a specific set of roles to a user based on role names.
     * Replaces any existing roles with the newly provided set of roles and logs the action in the audit log.
     *
     * @param userId the unique identifier of the user to receive the roles
     * @param roleNames a list containing the names of the roles to be assigned
     * @throws AppException if the user is not found by the provided ID
     */
    @Transactional
    public void assignRoles(Long userId, List<String> roleNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));
        
        List<Role> roles = roleRepository.findByNameIn(roleNames);
        user.setRoles(new HashSet<>(roles));
        userRepository.save(user);
        auditLogService.logAction(AuditLogAction.ASSIGN_ROLE, "Assigned roles to user ID: " + userId);
    }

    /**
     * Retrieves a paginated list of users whose registrations are currently pending approval.
     * Typically used by admins to vet new accounts before allowing them access.
     *
     * @param pageable the pagination parameters
     * @return a paginated response containing the list of pending UserResponseDTOs
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<UserResponseDTO> getPendingUsers(Pageable pageable) {
        Page<UserResponseDTO> page = userRepository.findByStatus(User.UserStatus.PENDING, pageable)
                .map(this::mapToDTO);
        return PaginatedResponse.from(page);
    }

    /**
     * Approves a user's pending registration, transitioning their status from PENDING to ACTIVE.
     * This grants them access to login and use the platform. It also sends a USER_APPROVED notification.
     * Example Notification: "Your account has been approved and activated."
     *
     * @param userId the unique identifier of the user to approve
     * @throws AppException if the user is not found or is not currently in a PENDING state
     */
    @Transactional
    public void approveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        if (user.getStatus() != User.UserStatus.PENDING) {
            throw new AppException("User is not in PENDING status", HttpStatus.BAD_REQUEST);
        }

        user.setStatus(User.UserStatus.ACTIVE);
        userRepository.save(user);

        auditLogService.logAction(AuditLogAction.APPROVE_USER, "Approved user ID: " + userId);
        notificationService.createForUser(user, "Your account has been approved and activated.", NotificationType.ACCOUNT_VALIDATED, user.getId());
    }

    /**
     * Permanently bans a user account, changing its status to BANNED.
     * This instantly invalidates all of the user's active refresh tokens, preventing new access tokens from being issued.
     *
     * @param userId the unique identifier of the user to ban
     * @throws AppException if the user cannot be found
     */
    @Transactional
    public void banUser(Long userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        user.setStatus(User.UserStatus.BANNED);
        user.setBanReason(reason);
        userRepository.save(user);

        refreshTokenService.deleteByUserId(userId);

        auditLogService.logAction(AuditLogAction.BAN_USER, "Banned user ID: " + userId + ". Reason: " + reason);
    }

    /**
     * Imposes a temporary timeout on a user account by setting a 'bannedUntil' timestamp.
     * Similar to a permanent ban, this destroys current refresh tokens, forcing a re-authentication attempt which will fail until the time expires.
     *
     * @param userId the unique identifier of the user to timeout
     * @param minutes the duration of the timeout in minutes
     * @throws AppException if the user cannot be found
     */
    @Transactional
    public void timeoutUser(Long userId, int minutes, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        user.setBannedUntil(LocalDateTime.now().plusMinutes(minutes));
        user.setBanReason(reason);
        userRepository.save(user);

        refreshTokenService.deleteByUserId(userId);

        auditLogService.logAction(AuditLogAction.TIMEOUT_USER, "Timed out user ID: " + userId + " for " + minutes + " minutes. Reason: " + reason);
    }

    private UserResponseDTO mapToDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .dateOfBirth(user.getDateOfBirth())
                .status(user.getStatus())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .bannedUntil(user.getBannedUntil())
                .banReason(user.getBanReason())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
