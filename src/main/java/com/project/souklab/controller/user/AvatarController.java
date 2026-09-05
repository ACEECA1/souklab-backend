package com.project.souklab.controller.user;

import com.project.souklab.dao.UserRepository;
import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.dto.user.AvatarResponseDTO;
import com.project.souklab.exception.BadRequestException;
import com.project.souklab.exception.ResourceNotFoundException;
import com.project.souklab.exception.UnauthorizedException;
import com.project.souklab.filestorage.config.StorageProperties;
import com.project.souklab.filestorage.exception.FileTooLargeException;
import com.project.souklab.model.User;
import com.project.souklab.service.user.AvatarService;
import com.project.souklab.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for authenticated user avatar management.
 * Provides multipart avatar upload, quota enforcement, and tier URL resolution.
 */
@RestController
@RequestMapping("/api/v1/users/me/avatars")
@RequiredArgsConstructor
@Slf4j
public class AvatarController {

    private final AvatarService avatarService;
    private final UserRepository userRepository;
    private final StorageProperties storageProperties;

    /**
     * Uploads, processes, and activates a new profile avatar for the currently authenticated user.
     *
     * @param file the multipart avatar image file
     * @return 201 Created containing AvatarResponseDTO with URLs for all resolution tiers
     * @throws UnauthorizedException if no authenticated user is present
     * @throws BadRequestException if the file is missing or empty
     * @throws FileTooLargeException if the file size exceeds the configured avatar maximum
     * @throws ResourceNotFoundException if the authenticated user record is not found
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AvatarResponseDTO>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) {
            throw new UnauthorizedException("User is not authenticated");
        }

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Avatar file is required and cannot be empty");
        }

        if (storageProperties.getValidation() != null && storageProperties.getValidation().getMaxFileSize() != null) {
            long maxBytes = storageProperties.getValidation().getMaxFileSize().toBytes();
            if (file.getSize() > maxBytes) {
                throw new FileTooLargeException(file.getSize(), maxBytes);
            }
        }

        User currentUser = userRepository.findByEmail(username.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        AvatarResponseDTO response = avatarService.uploadAvatar(currentUser, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Avatar uploaded successfully"));
    }
}
