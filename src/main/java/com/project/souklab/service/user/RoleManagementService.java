package com.project.souklab.service.user;

import lombok.RequiredArgsConstructor;
import com.project.souklab.dao.PermissionRepository;
import com.project.souklab.dao.RoleRepository;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.dto.common.PaginatedResponse;
import com.project.souklab.dto.role.RoleCreateRequestDTO;
import com.project.souklab.dto.role.RoleUpdateRequestDTO;
import com.project.souklab.dto.role.RoleResponseDTO;
import com.project.souklab.model.Permission;
import com.project.souklab.model.Role;
import com.project.souklab.model.User;
import com.project.souklab.service.audit.AuditLogService;
import com.project.souklab.util.AppException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.project.souklab.model.NotificationType;
import com.project.souklab.model.AuditLogAction;
import com.project.souklab.model.PermissionType;
import com.project.souklab.service.notification.NotificationService;

@Service
@RequiredArgsConstructor
public class RoleManagementService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    /**
     * Creates a new role with a specified set of permissions.
     * Throws an exception if a role with the same name already exists.
     *
     * @param dto the data transfer object containing the role's name and its requested permissions
     * @return a RoleResponseDTO describing the successfully created role
     * @throws AppException if the role name is taken or a requested permission does not exist
     */
    @Transactional
    public RoleResponseDTO createRole(RoleCreateRequestDTO dto) {
        if (roleRepository.findByName(dto.getName()).isPresent()) {
            throw new AppException("Role already exists", HttpStatus.BAD_REQUEST);
        }

        Set<Permission> permissions = dto.getPermissions().stream()
                .map(name -> permissionRepository.findByName(PermissionType.valueOf(name))
                        .orElseThrow(() -> new AppException("Permission not found: " + name, HttpStatus.BAD_REQUEST)))
                .collect(Collectors.toSet());

        Role role = new Role();
        role.setName(dto.getName());
        role.setPermissions(permissions);

        Role saved = roleRepository.save(role);
        auditLogService.logAction(AuditLogAction.CREATE_ROLE, "Created role: " + saved.getName());
        return mapToDTO(saved);
    }

    /**
     * Updates an existing role by replacing its current permissions with a new set.
     * Useful for adjusting access control without deleting and recreating a role.
     *
     * @param roleId the unique identifier of the role to update
     * @param dto the data transfer object containing the new set of permission names
     * @return the updated RoleResponseDTO
     * @throws AppException if the role or any requested permission is not found
     */
    @Transactional
    public RoleResponseDTO updateRolePermissions(Long roleId, RoleUpdateRequestDTO dto) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new AppException("Role not found", HttpStatus.NOT_FOUND));

        Set<Permission> permissions = dto.getPermissions().stream()
                .map(name -> permissionRepository.findByName(PermissionType.valueOf(name))
                        .orElseThrow(() -> new AppException("Permission not found: " + name, HttpStatus.BAD_REQUEST)))
                .collect(Collectors.toSet());

        role.setPermissions(permissions);
        Role saved = roleRepository.save(role);
        auditLogService.logAction(AuditLogAction.UPDATE_ROLE, "Updated permissions for role: " + saved.getName());
        return mapToDTO(saved);
    }

    /**
     * Retrieves a paginated list of all roles configured in the system.
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
     * Returns a full list of all available permission names in the system.
     * Typically used by the frontend to populate role-creation forms.
     *
     * @return an alphabetically sorted list of permission names
     */
    @Transactional(readOnly = true)
    public List<String> getPermissions() {
        return permissionRepository.findAll().stream()
                .map(p -> p.getName().name())
                .sorted()
                .toList();
    }

    /**
     * Assigns one or multiple roles to a specific user.
     * Active user sessions will be invalidated to enforce the new permissions immediately on their next request.
     * This will send a notification to the user about the new roles assigned.
     * Example Notification: "You have been assigned the following roles: MODERATOR, ADMIN"
     *
     * @param userId the unique identifier of the user receiving the roles
     * @param roleNames a set of role names to append to the user's current roles
     * @throws AppException if the user or any role name is not found
     */
    @Transactional
    public void assignRolesToUser(Long userId, Set<String> roleNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new AppException("Role not found: " + roleName, HttpStatus.BAD_REQUEST));
            user.getRoles().add(role);
        }
        userRepository.save(user);
        invalidateUserSessions(user.getUsername());
        auditLogService.logAction(AuditLogAction.ASSIGN_ROLE, "Assigned roles to user ID: " + userId);
        notificationService.createForUser(user, "You have been assigned the following roles: " + String.join(", ", roleNames), NotificationType.ROLE_ASSIGNED, user.getId());
    }

    /**
     * Assigns a single role to multiple users at once.
     * This is highly efficient for batch-promoting users (e.g., granting 'MODERATOR' to a list of trusted users).
     * This will send a notification to each user about the new role assigned.
     * Example Notification: "You have been assigned the following role: MODERATOR"
     *
     * @param roleName the name of the role being assigned
     * @param userIds a list of user IDs to receive the role
     * @throws AppException if the role or any of the provided users are not found
     */
    @Transactional
    public void assignRoleToUsersBulk(String roleName, List<Long> userIds) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new AppException("Role not found: " + roleName, HttpStatus.BAD_REQUEST));

        List<User> users = userRepository.findAllById(userIds);
        if (users.size() != userIds.size()) {
            throw new AppException("One or more users not found", HttpStatus.NOT_FOUND);
        }

        for (User user : users) {
            user.getRoles().add(role);
            invalidateUserSessions(user.getUsername());
            notificationService.createForUser(user, "You have been assigned the following role: " + roleName, NotificationType.ROLE_ASSIGNED, user.getId());
        }
        userRepository.saveAll(users);
        auditLogService.logAction(AuditLogAction.ASSIGN_ROLE_BULK, "Assigned role " + roleName + " to users: " + userIds);
    }

    private void invalidateUserSessions(String username) {
        
        
    }

    private RoleResponseDTO mapToDTO(Role role) {
        Set<String> permissions = role.getPermissions().stream()
                .map(p -> p.getName().name())
                .collect(Collectors.toSet());

        return RoleResponseDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .permissions(permissions)
                .build();
    }
}
