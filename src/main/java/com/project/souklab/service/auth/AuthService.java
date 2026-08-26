package com.project.souklab.service.auth;

import lombok.RequiredArgsConstructor;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.dto.auth.*;
import com.project.souklab.model.RefreshToken;
import com.project.souklab.model.Role;
import com.project.souklab.model.User;
import com.project.souklab.security.JwtUtils;
import com.project.souklab.service.notification.NotificationService;
import com.project.souklab.service.security.RefreshTokenService;
import com.project.souklab.exception.AppException;
import com.project.souklab.util.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final NotificationService notificationService;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;

    /**
     * Registers a new user account in the system.
     * The user's status is initially set to PENDING, requiring an admin to approve the registration.
     * It also triggers a notification to all admins that a new user is awaiting approval.
     *
     * @param dto the data transfer object containing the user's registration details (username, password, etc.)
     * @return the newly created and saved User entity
     * @throws AppException if the provided username is already taken
     */
    @Transactional
    public User registerUser(UserRegistrationDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new AppException("Username is already taken", HttpStatus.BAD_REQUEST);
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setDateOfBirth(dto.getDateOfBirth());
        user.setStatus(User.UserStatus.PENDING);

        User savedUser = userRepository.save(user);
        notificationService.notifyAdmins("New user registration pending approval: " + savedUser.getUsername());
        return savedUser;
    }

    /**
     * Authenticates a user based on their credentials and generates access/refresh tokens.
     * If authentication is successful, the user's context is set in the SecurityContextHolder.
     *
     * @param dto the data transfer object containing the user's login credentials (username and password)
     * @return a JwtResponseDTO containing the JWT access token, refresh token, and user role information
     * @throws AppException if the user is not found or authentication fails
     */
    @Transactional
    public JwtResponseDTO login(LoginDTO dto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateAccessToken(authentication);
        
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return JwtResponseDTO.builder()
                .accessToken(jwt)
                .refreshToken(refreshToken.getToken())
                .username(user.getUsername())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                .build();
    }

    /**
     * Refreshes a user's JWT access token using a valid refresh token.
     * The refresh token must exist in the database and not be expired.
     *
     * @param request the data transfer object containing the user's current refresh token
     * @return a new JwtResponseDTO containing the newly generated access token and the same refresh token
     * @throws AppException if the refresh token is missing, invalid, or expired
     */
    @Transactional
    public JwtResponseDTO refreshToken(TokenRefreshRequestDTO request) {
        return refreshTokenService.findByToken(request.getRefreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateAccessToken(user.getUsername());
                    return JwtResponseDTO.builder()
                            .accessToken(token)
                            .refreshToken(request.getRefreshToken())
                            .username(user.getUsername())
                            .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                            .build();
                })
                .orElseThrow(() -> new AppException("Refresh token is not in database!", HttpStatus.FORBIDDEN));
    }

    /**
     * Logs out a user by invalidating their current session and deleting all their active refresh tokens.
     *
     * @param username the username of the user to log out
     */
    @Transactional
    public void logout(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null) {
            refreshTokenService.deleteByUserId(user.getId());
        }
    }

    /**
     * Changes the authenticated user's password securely.
     * The user must provide their correct old password to authorize the change.
     *
     * @param request the data transfer object containing the old password and the new desired password
     * @throws AppException if the user is not authenticated, not found, or the old password does not match
     */
    @Transactional
    public void changePassword(ChangePasswordRequestDTO request) {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) {
            throw new AppException("Not authenticated", HttpStatus.UNAUTHORIZED);
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppException("Invalid old password", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * Initiates a password reset process for a user.
     * Token-based email reset will be implemented in Phase 2.
     *
     * @param request the data transfer object containing the username/email of the account
     */
    @Transactional
    public void forgotPassword(ForgotPasswordRequestDTO request) {
        // Phase 2 implementation will generate a secure reset token and email it to the user
    }

    /**
     * Completes the password reset process by applying the new password.
     * Token-based email reset will be implemented in Phase 2.
     *
     * @param request the data transfer object containing the reset token and the new password
     */
    @Transactional
    public void resetPassword(ResetPasswordRequestDTO request) {
        // Phase 2 implementation will validate token and update password
    }

    /**
     * Retrieves the profile and details of the currently authenticated user.
     *
     * @return a UserResponseDTO containing the user's basic profile, roles, and permissions
     * @throws AppException if there is no authenticated user or the user cannot be found
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getCurrentUser() {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) {
            throw new AppException("Not authenticated", HttpStatus.UNAUTHORIZED);
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        return mapToDTO(user);
    }

    /**
     * Updates the profile information of the currently authenticated user.
     * Only fields that are explicitly provided (non-null) in the request will be modified.
     *
     * @param request the data transfer object containing the profile fields to update
     * @return a {@link UserResponseDTO} containing the user's updated profile information
     * @throws AppException if the user is not authenticated or cannot be found in the database
     */
    @Transactional
    public UserResponseDTO updateProfile(UpdateProfileRequestDTO request) {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) {
            throw new AppException("Not authenticated", HttpStatus.UNAUTHORIZED);
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getDateOfBirth() != null) user.setDateOfBirth(request.getDateOfBirth());

        userRepository.save(user);
        return mapToDTO(user);
    }

    /**
     * Converts a User entity into a UserResponseDTO, flattening roles into collections.
     *
     * @param user the User entity to map
     * @return the mapped UserResponseDTO
     */
    public UserResponseDTO mapToDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .dateOfBirth(user.getDateOfBirth())
                .status(user.getStatus())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .build();
    }
}
