package com.project.souklab.service.user;

import com.project.souklab.dao.RoleRepository;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.dto.auth.UserResponseDTO;
import com.project.souklab.dto.common.PaginatedResponse;
import com.project.souklab.exception.BadRequestException;
import com.project.souklab.exception.ResourceNotFoundException;
import com.project.souklab.model.AccountStatus;
import com.project.souklab.model.AuditLogAction;
import com.project.souklab.model.NotificationType;
import com.project.souklab.model.Role;
import com.project.souklab.model.User;
import com.project.souklab.service.audit.AuditLogService;
import com.project.souklab.service.notification.NotificationService;
import com.project.souklab.service.security.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

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
     * @param search optional search query for filtering by email or name
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
     * @throws ResourceNotFoundException if the user is not found by the provided ID
     */
    @Transactional
    public void assignRoles(String userId, List<String> roleNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        List<Role> roles = roleRepository.findByNameIn(roleNames);
        user.setRoles(new HashSet<>(roles));
        userRepository.save(user);
        auditLogService.logAction(AuditLogAction.ASSIGN_ROLE, "Assigned roles to user ID: " + userId);
    }

    /**
     * Retrieves a paginated list of users whose registrations are currently pending approval.
     * Typically used by admins to vet new artisan accounts before activating them.
     *
     * @param pageable the pagination parameters
     * @return a paginated response containing the list of pending UserResponseDTOs
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<UserResponseDTO> getPendingUsers(Pageable pageable) {
        Page<UserResponseDTO> page = userRepository.findByStatus(AccountStatus.PENDING, pageable)
                .map(this::mapToDTO);
        return PaginatedResponse.from(page);
    }

    /**
     * Approves a user's pending registration, transitioning their status from PENDING to ACTIVE.
     * This grants them full access to the platform and sends a validation notification.
     *
     * @param userId the unique identifier of the user to approve
     * @throws ResourceNotFoundException if the user is not found
     * @throws BadRequestException if the user is not in PENDING status
     */
    @Transactional
    public void approveUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getStatus() != AccountStatus.PENDING) {
            throw new BadRequestException("User is not in PENDING status");
        }

        user.setStatus(AccountStatus.ACTIVE);
        userRepository.save(user);

        auditLogService.logAction(AuditLogAction.APPROVE_USER, "Approved user ID: " + userId);
        notificationService.createForUser(user, "Your account has been approved and activated.", NotificationType.ACCOUNT_VALIDATED, user.getId());
    }

    /**
     * Suspends a user account, changing its status to SUSPENDED.
     * This instantly invalidates all active refresh tokens, preventing token refreshes.
     *
     * @param userId the unique identifier of the user to ban/suspend
     * @param reason the reason for suspension
     * @throws ResourceNotFoundException if the user cannot be found
     */
    @Transactional
    public void banUser(String userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setStatus(AccountStatus.SUSPENDED);
        user.setBanReason(reason);
        userRepository.save(user);

        refreshTokenService.deleteByUserId(userId);

        auditLogService.logAction(AuditLogAction.BAN_USER, "Banned user ID: " + userId + ". Reason: " + reason);
    }

    /**
     * Imposes a temporary timeout on a user account by setting a 'bannedUntil' timestamp.
     * Invalidates active refresh tokens, preventing authentication until the timeout expires.
     *
     * @param userId the unique identifier of the user to timeout
     * @param minutes the duration of the timeout in minutes
     * @param reason the reason for timeout
     * @throws ResourceNotFoundException if the user cannot be found
     */
    @Transactional
    public void timeoutUser(String userId, int minutes, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setBannedUntil(LocalDateTime.now().plusMinutes(minutes));
        user.setBanReason(reason);
        userRepository.save(user);

        refreshTokenService.deleteByUserId(userId);

        auditLogService.logAction(AuditLogAction.TIMEOUT_USER, "Timed out user ID: " + userId + " for " + minutes + " minutes. Reason: " + reason);
    }

    private UserResponseDTO mapToDTO(User user) {
        String primaryRole = user.getRoles().stream()
                .findFirst()
                .map(Role::getName)
                .orElse("ROLE_CLIENT");

        boolean isTeacher = user.getArtisanProfile() != null && user.getArtisanProfile().isTeacher();
        boolean isPremium = (user.getArtisanProfile() != null && user.getArtisanProfile().isPremium())
                || (user.getClient() != null && user.getClient().isPremium());
        boolean isValidated = (user.getArtisanProfile() != null && user.getArtisanProfile().isVerified())
                || (user.getClient() != null && user.getClient().isVerified());

        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .name(user.getName())
                .status(user.getStatus())
                .primaryRole(primaryRole)
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .isPremium(isPremium)
                .isValidated(isValidated)
                .isTeacher(isTeacher)
                .bannedUntil(user.getBannedUntil())
                .banReason(user.getBanReason())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
