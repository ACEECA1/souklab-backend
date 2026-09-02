package com.project.souklab.controller.auth;

import tools.jackson.databind.JsonNode;
import com.project.souklab.dto.auth.*;
import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.dto.profile.ProfileResponse;
import com.project.souklab.model.AccountStatus;
import com.project.souklab.service.auth.AuthService;
import com.project.souklab.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<ProfileResponse>> register(@Valid @RequestBody UserRegistrationDTO registrationDTO) {
        ProfileResponse response = authService.registerUser(registrationDTO);
        // Determine status message based on the user's account status
        boolean isPending = response.getAccountStatus() == AccountStatus.PENDING;
        String message = isPending
                ? "Registration successful. Your artisan account has been created and is pending administrator verification."
                : "Registration successful. Welcome to Souklab!";

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, message));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponseDTO>> login(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        JwtResponseDTO response = authService.login(loginDTO, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful."));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtResponseDTO>> refreshToken(@Valid @RequestBody TokenRefreshRequestDTO request) {
        JwtResponseDTO response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully."));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestBody(required = false) TokenRefreshRequestDTO tokenRequest,
            HttpServletRequest request) {
        String userEmail = SecurityUtils.getCurrentUsername();
        String refreshToken = tokenRequest != null ? tokenRequest.getRefreshToken() : null;
        authService.logout(userEmail, refreshToken);
        return ResponseEntity.ok(ApiResponse.success(null, "Logout successful."));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody VerifyEmailRequestDTO request) {
        authService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Email verified successfully."));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(@Valid @RequestBody ResendVerificationRequestDTO request) {
        authService.resendVerification(request);
        return ResponseEntity.ok(ApiResponse.success(null, "If an unverified account exists for this email, a verification code has been sent."));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "If an account exists for this email, instructions have been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset successfully. You can now log in with your new password."));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequestDTO request) {
        authService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully."));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> getCurrentUser() {
        return ResponseEntity.ok(ApiResponse.success(authService.getCurrentUser()));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> patchCurrentUser(@RequestBody(required = false) JsonNode requestBody) {
        ProfileResponse response = authService.patchCurrentUser(requestBody);
        return ResponseEntity.ok(ApiResponse.success(response, "Profile updated successfully."));
    }

    @PostMapping("/complete-profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> completeProfile(@Valid @RequestBody CompleteProfileRequestDTO request) {
        ProfileResponse response = authService.completeProfile(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Profile completed successfully."));
    }

    @GetMapping("/oauth/google/artisan")
    public void initiateGoogleOAuthArtisan(HttpServletResponse response) throws IOException {
        setIntentCookie(response, "ROLE_ARTISAN");
        response.sendRedirect("/oauth2/authorization/google");
    }

    @GetMapping("/oauth/google/client")
    public void initiateGoogleOAuthClient(HttpServletResponse response) throws IOException {
        setIntentCookie(response, "ROLE_CLIENT");
        response.sendRedirect("/oauth2/authorization/google");
    }

    private void setIntentCookie(HttpServletResponse response, String intentRole) {
        ResponseCookie cookie = ResponseCookie.from("SOUKLAB_OAUTH_INTENT", intentRole)
                .path("/")
                .httpOnly(true)
                .sameSite("Lax")
                .maxAge(300) // 5 minutes
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
