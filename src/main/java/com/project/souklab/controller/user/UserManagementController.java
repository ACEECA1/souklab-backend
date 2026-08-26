package com.project.souklab.controller.user;

import lombok.RequiredArgsConstructor;
import com.project.souklab.dto.auth.UserResponseDTO;
import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.dto.common.PaginatedResponse;
import com.project.souklab.service.user.UserManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import com.project.souklab.dto.user.BanRequestDTO;
import java.util.List;
import com.project.souklab.dto.user.TimeoutRequestDTO;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementController {

    private final UserManagementService userManagementService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<UserResponseDTO>>> getAllUsers(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(userManagementService.getAllUsers(search, pageable)));
    }

    @PutMapping("/{id}/roles")
    public ResponseEntity<ApiResponse<Void>> assignRoles(@PathVariable Long id, @RequestBody List<String> roleNames) {
        userManagementService.assignRoles(id, roleNames);
        return ResponseEntity.ok(ApiResponse.success(null, "Roles assigned successfully"));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<PaginatedResponse<UserResponseDTO>>> getPendingUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(userManagementService.getPendingUsers(pageable)));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approveUser(@PathVariable Long id) {
        userManagementService.approveUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User approved successfully"));
    }

    @PostMapping("/approve-bulk")
    public ResponseEntity<ApiResponse<Void>> approveUsersBulk(@RequestBody List<Long> ids) {
        for(Long id : ids) {
            userManagementService.approveUser(id);
        }
        return ResponseEntity.ok(ApiResponse.success(null, "Users approved successfully"));
    }

    @PostMapping("/{id}/ban")
    public ResponseEntity<ApiResponse<Void>> banUser(@PathVariable Long id, @RequestBody BanRequestDTO request) {
        userManagementService.banUser(id, request.getReason());
        return ResponseEntity.ok(ApiResponse.success(null, "User banned successfully"));
    }

    @PostMapping("/{id}/timeout")
    public ResponseEntity<ApiResponse<Void>> timeoutUser(@PathVariable Long id, @RequestBody TimeoutRequestDTO request) {
        userManagementService.timeoutUser(id, request.getMinutes(), request.getReason());
        return ResponseEntity.ok(ApiResponse.success(null, "User timed out successfully"));
    }
}
