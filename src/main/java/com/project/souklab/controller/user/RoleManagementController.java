package com.project.souklab.controller.user;

import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.dto.common.PaginatedResponse;
import com.project.souklab.dto.role.AssignRoleRequestDTO;
import com.project.souklab.dto.role.BulkAssignRoleRequestDTO;
import com.project.souklab.dto.role.RoleResponseDTO;
import com.project.souklab.service.user.RoleManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RoleManagementController {

    private final RoleManagementService roleManagementService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<RoleResponseDTO>>> getRoles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(roleManagementService.getRoles(PageRequest.of(page, size))));
    }

    @PostMapping("/assign")
    public ResponseEntity<ApiResponse<Void>> assignRoleToUser(@Valid @RequestBody AssignRoleRequestDTO request) {
        roleManagementService.assignRoleToUser(request.getUserId(), request.getRoleName());
        return ResponseEntity.ok(ApiResponse.success(null, "Role assigned successfully"));
    }

    @PostMapping("/assign-bulk")
    public ResponseEntity<ApiResponse<Void>> assignRoleToUsersBulk(@Valid @RequestBody BulkAssignRoleRequestDTO request) {
        roleManagementService.assignRoleToUsersBulk(request.getRoleName(), request.getUserIds());
        return ResponseEntity.ok(ApiResponse.success(null, "Role assigned to users successfully"));
    }
}
