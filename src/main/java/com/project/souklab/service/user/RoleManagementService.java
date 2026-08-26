package com.project.souklab.service.user;

import com.project.souklab.dao.RoleRepository;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.dto.common.PaginatedResponse;
import com.project.souklab.dto.role.RoleCreateRequestDTO;
import com.project.souklab.dto.role.RoleResponseDTO;
import com.project.souklab.dto.role.RoleUpdateRequestDTO;
import com.project.souklab.exception.BadRequestException;
import com.project.souklab.exception.ConflictException;
import com.project.souklab.exception.ResourceNotFoundException;
import com.project.souklab.model.AuditLogAction;
import com.project.souklab.model.Role;
import com.project.souklab.model.User;
import com.project.souklab.service.audit.AuditLogService;
import com.project.souklab.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleManagementService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    /**
     * Creates a new role.
     * Throws an exception if a role with the same name already exists.
     *
     * @param dto the data transfer object containing the role's name and description
     * @return a RoleResponseDTO describing the successfully created role
     * @throws ConflictException if the role name is taken
     */
    @Transactional
    public RoleResponseDTO createRole(RoleCreateRequestDTO dto) {
        if (roleRepository.findByName(dto.getName()).isPresent()) {
            throw new ConflictException("Role already exists: " + dto.getName());
        }

        Role role = new Role();
        role.setName(dto.getName());
        role.setDescription(dto.getDescription());

        Role saved = roleRepository.save(role);
        auditLogService.logAction(AuditLogAction.CREATE_ROLE, "Created role: " + saved.getName());
        return mapToDTO(saved);
    }

    /**
     * Updates an existing role's description.
     *
     * @param roleId the unique identifier of the role to update
     * @param dto the data transfer object containing the updated fields
     * @return the updated RoleResponseDTO
     * @throws ResourceNotFoundException if the role is not found
     */
    @Transactional
    public RoleResponseDTO updateRole(String roleId, RoleUpdateRequestDTO dto) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        if (dto.getDescription() != null) {
            role.setDescription(dto.getDescription());
        }
        Role saved = roleRepository.save(role);
        auditLogService.logAction(AuditLogAction.UPDATE_ROLE, "Updated role: " + saved.getName());
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
     * Assigns one or multiple roles to a specific user.
     *
     * @param userId the unique identifier of the user receiving the roles
     * @param roleNames a set of role names to append to the user's current roles
     * @throws ResourceNotFoundException if the user is not found
     * @throws BadRequestException if any role name is not found
     */
    @Transactional
    public void assignRolesToUser(String userId, Set<String> roleNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new BadRequestException("Role not found: " + roleName));
            user.getRoles().add(role);
        }
        userRepository.save(user);
        auditLogService.logAction(AuditLogAction.ASSIGN_ROLE, "Assigned roles to user ID: " + userId);
        notificationService.createForUser(user, "You have been assigned the following roles: " + String.join(", ", roleNames));
    }

    /**
     * Assigns a single role to multiple users at once.
     *
     * @param roleName the name of the role being assigned
     * @param userIds a list of user IDs to receive the role
     * @throws BadRequestException if the role is not found
     * @throws ResourceNotFoundException if any of the provided users are not found
     */
    @Transactional
    public void assignRoleToUsersBulk(String roleName, List<String> userIds) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new BadRequestException("Role not found: " + roleName));

        List<User> users = userRepository.findAllById(userIds);
        if (users.size() != userIds.size()) {
            throw new ResourceNotFoundException("One or more users not found");
        }

        for (User user : users) {
            user.getRoles().add(role);
            notificationService.createForUser(user, "You have been assigned the following role: " + roleName);
        }
        userRepository.saveAll(users);
        auditLogService.logAction(AuditLogAction.ASSIGN_ROLE_BULK, "Assigned role " + roleName + " to users: " + userIds);
    }

    private RoleResponseDTO mapToDTO(Role role) {
        return RoleResponseDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .build();
    }
}
