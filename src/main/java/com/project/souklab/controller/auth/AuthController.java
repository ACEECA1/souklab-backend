package com.project.souklab.controller.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.project.souklab.dto.auth.*;
import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.service.auth.AuthService;
import com.project.souklab.util.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody UserRegistrationDTO registrationDTO) {
        authService.registerUser(registrationDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(null, "Registration successful. Please wait for an administrator to approve your account."));
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponseDTO>> login(@Valid @RequestBody LoginDTO loginDTO) {
        JwtResponseDTO response = authService.login(loginDTO);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtResponseDTO>> refreshToken(@Valid @RequestBody TokenRefreshRequestDTO request) {
        JwtResponseDTO response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        String username = SecurityUtils.getCurrentUsername();
        if (username != null) {
            authService.logout(username);
        }
        return ResponseEntity.ok(ApiResponse.success(null, "Logout successful"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getCurrentUser() {
        return ResponseEntity.ok(ApiResponse.success(authService.getCurrentUser()));
    }

    @PostMapping("/update-profile")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateProfile(@Valid @RequestBody UpdateProfileRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(authService.updateProfile(request), "Profile updated successfully"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequestDTO request) {
        authService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset initiated successfully."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset successfully. You may now login."));
    }
}
