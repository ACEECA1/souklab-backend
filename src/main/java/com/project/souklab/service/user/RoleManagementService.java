package com.project.souklab.service.user;

import com.project.souklab.dao.ArtisanProfileRepository;
import com.project.souklab.dao.ClientRepository;
import com.project.souklab.dao.RoleRepository;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.dto.common.PaginatedResponse;
import com.project.souklab.dto.role.RoleResponseDTO;
import com.project.souklab.exception.BadRequestException;
import com.project.souklab.exception.ResourceNotFoundException;
import com.project.souklab.model.*;
import com.project.souklab.service.audit.AuditLogService;
import com.project.souklab.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleManagementService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ArtisanProfileRepository artisanProfileRepository;
    private final ClientRepository clientRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    /**
     * Retrieves a paginated list of all system roles (ROLE_ADMIN, ROLE_ARTISAN, ROLE_CLIENT).
     *
     * @param pageable the pagination parameters
     * @return a paginated response of role DTOs
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<RoleResponseDTO> getRoles(Pageable pageable) {
        Page<RoleResponseDTO> page = roleRepository.findAll(pageable)
                .map(this::mapToDTO);
        return PaginatedResponse.from(page);
    }

    /**
     * Safely assigns a single, mutually exclusive role to a user.
     * Replaces any existing roles and ensures corresponding profile rows (ArtisanProfile or Client)
     * exist so the user is in a consistent state without altering AccountStatus.
     *
     * @param userId the unique identifier of the user receiving the role
     * @param roleInput the target role name (e.g. ROLE_ARTISAN, ROLE_CLIENT, ROLE_ADMIN)
     * @throws ResourceNotFoundException if the user or role is not found
     * @throws BadRequestException if the role name is invalid
     */
    @Transactional
    public void assignRoleToUser(String userId, String roleInput) {
        String roleName = normalizeRoleName(roleInput);

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setRoles(new HashSet<>(Set.of(role)));

        if (roleName.equals("ROLE_ARTISAN")) {
            if (user.getArtisanProfile() == null && artisanProfileRepository.findById(userId).isEmpty()) {
                ArtisanProfile profile = ArtisanProfile.builder()
                        .user(user)
                        .build();
                artisanProfileRepository.save(profile);
                user.setArtisanProfile(profile);
            }
        } else if (roleName.equals("ROLE_CLIENT")) {
            if (user.getClient() == null && clientRepository.findById(userId).isEmpty()) {
                Client client = Client.builder()
                        .user(user)
                        .build();
                clientRepository.save(client);
                user.setClient(client);
            }
        }

        userRepository.save(user);

        auditLogService.logAction(AuditLogAction.ASSIGN_ROLE, "Assigned role " + roleName + " to user ID: " + userId);
        notificationService.createForUser(user, "Your account role has been updated to: " + roleName);
    }

    /**
     * Reassigns a single role to multiple users at once.
     *
     * @param roleInput the name of the role being assigned
     * @param userIds a list of user IDs to receive the role
     * @throws BadRequestException if the role name is invalid
     * @throws ResourceNotFoundException if any of the provided users are not found
     */
    @Transactional
    public void assignRoleToUsersBulk(String roleInput, List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            throw new BadRequestException("At least one user ID is required");
        }

        String roleName = normalizeRoleName(roleInput);

        for (String userId : userIds) {
            assignRoleToUser(userId, roleName);
        }

        auditLogService.logAction(AuditLogAction.ASSIGN_ROLE_BULK, "Assigned role " + roleName + " to users: " + userIds);
    }

    private String normalizeRoleName(String roleInput) {
        if (roleInput == null || roleInput.isBlank()) {
            throw new BadRequestException("Role name is required");
        }
        String roleName = roleInput.trim().toUpperCase();
        if (!roleName.startsWith("ROLE_")) {
            roleName = "ROLE_" + roleName;
        }
        if (!roleName.equals("ROLE_ADMIN") && !roleName.equals("ROLE_ARTISAN") && !roleName.equals("ROLE_CLIENT")) {
            throw new BadRequestException("Invalid role: " + roleInput + ". Allowed roles are ROLE_ADMIN, ROLE_ARTISAN, or ROLE_CLIENT.");
        }
        return roleName;
    }

    private RoleResponseDTO mapToDTO(Role role) {
        return RoleResponseDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .build();
    }
}
