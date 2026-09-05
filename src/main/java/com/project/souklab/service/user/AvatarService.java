package com.project.souklab.service.user;

import com.project.souklab.dao.UserAvatarRepository;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.dto.user.AvatarResponseDTO;
import com.project.souklab.exception.AvatarLimitExceededException;
import com.project.souklab.exception.BadRequestException;
import com.project.souklab.filestorage.StorageResult;
import com.project.souklab.filestorage.StorageService;
import com.project.souklab.filestorage.exception.StorageException;
import com.project.souklab.filestorage.image.ImageProcessingService;
import com.project.souklab.filestorage.image.ImageVariant;
import com.project.souklab.filestorage.image.ResolutionTier;
import com.project.souklab.filestorage.scan.VirusScanService;
import com.project.souklab.filestorage.validation.FileValidator;
import com.project.souklab.filestorage.validation.ValidatedFile;
import com.project.souklab.model.User;
import com.project.souklab.model.UserAvatar;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Orchestrator service managing user avatar lifecycle and gallery storage.
 * Enforces per-user gallery quotas, executes input validation, virus scanning, multi-tier
 * image processing, storage persistence with rollback compensation, and transactional activation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AvatarService {

    /**
     * Maximum allowed avatars stored per user in gallery history.
     */
    public static final long MAX_AVATARS_PER_USER = 10L;

    /**
     * URL path prefix for serving files through the application's file-serving endpoint.
     */
    public static final String FILE_SERVING_PATH_PREFIX = "/api/v1/files/";

    /**
     * Strict set of MIME types allowed exclusively for user avatar uploads.
     */
    public static final List<String> AVATAR_ALLOWED_MIME_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final UserAvatarRepository userAvatarRepository;
    private final UserRepository userRepository;
    private final FileValidator fileValidator;
    private final VirusScanService virusScanService;
    private final ImageProcessingService imageProcessingService;
    private final StorageService storageService;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;

    /**
     * Uploads, processes, stores, and activates a new avatar for the authenticated user.
     * Execution pipeline follows strict sequencing:
     * <ol>
     *   <li>Quota verification (must not exceed 10 avatars).</li>
     *   <li>File validation and sanitization against image-only MIME constraints.</li>
     *   <li>Antivirus scanning.</li>
     *   <li>Three-tier image variant generation (Original, Medium, Thumbnail).</li>
     *   <li>Storage persistence across all 3 tiers with compensating cleanup on failure.</li>
     *   <li>Transactional database persistence, active avatar switching, and User profile synchronization.</li>
     * </ol>
     *
     * @param currentUser the authenticated user uploading the avatar
     * @param file the multipart avatar image payload
     * @return AvatarResponseDTO containing image URLs for all three resolution tiers
     * @throws BadRequestException if the file is missing or empty
     * @throws AvatarLimitExceededException if the user has reached the 10-avatar limit
     * @throws StorageException if variant generation or storage operations fail
     */
    public AvatarResponseDTO uploadAvatar(User currentUser, MultipartFile file) {
        if (currentUser == null) {
            throw new IllegalArgumentException("Current user cannot be null");
        }
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Avatar file is required and cannot be empty");
        }

        long existingCount = userAvatarRepository.countByUserId(currentUser.getId());
        if (existingCount >= MAX_AVATARS_PER_USER) {
            log.warn("Avatar upload rejected for user {}: quota limit of {} avatars reached",
                    currentUser.getId(), MAX_AVATARS_PER_USER);
            throw new AvatarLimitExceededException(
                    "Maximum avatar limit of " + MAX_AVATARS_PER_USER + " reached. Please delete an existing avatar before uploading a new one."
            );
        }

        ValidatedFile validatedFile;
        try (InputStream stream = file.getInputStream()) {
            validatedFile = fileValidator.validateAndSanitize(
                    stream,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    AVATAR_ALLOWED_MIME_TYPES
            );
        } catch (IOException e) {
            log.error("Failed to read avatar upload stream for user {}", currentUser.getId(), e);
            throw new StorageException("Failed to read uploaded avatar stream: " + e.getMessage(), e);
        }

        ValidatedFile scannedFile = virusScanService.scan(validatedFile);

        Map<ResolutionTier, ImageVariant> variants = imageProcessingService.generateVariants(scannedFile);
        ImageVariant originalVariant = variants.get(ResolutionTier.ORIGINAL);
        ImageVariant mediumVariant = variants.get(ResolutionTier.MEDIUM);
        ImageVariant thumbnailVariant = variants.get(ResolutionTier.THUMBNAIL);

        if (originalVariant == null || mediumVariant == null || thumbnailVariant == null) {
            throw new StorageException("Image processing failed to generate all required resolution tiers");
        }

        List<String> storedKeys = new ArrayList<>(3);
        String originalKey;
        String mediumKey;
        String thumbnailKey;

        try {
            StorageResult origResult = storageService.store(
                    originalVariant.getInputStream(),
                    scannedFile.sanitizedFilename(),
                    originalVariant.contentType(),
                    originalVariant.getSize()
            );
            originalKey = origResult.key();
            storedKeys.add(originalKey);

            StorageResult medResult = storageService.store(
                    mediumVariant.getInputStream(),
                    scannedFile.sanitizedFilename(),
                    mediumVariant.contentType(),
                    mediumVariant.getSize()
            );
            mediumKey = medResult.key();
            storedKeys.add(mediumKey);

            StorageResult thumbResult = storageService.store(
                    thumbnailVariant.getInputStream(),
                    scannedFile.sanitizedFilename(),
                    thumbnailVariant.contentType(),
                    thumbnailVariant.getSize()
            );
            thumbnailKey = thumbResult.key();
            storedKeys.add(thumbnailKey);

            return transactionTemplate.execute(status -> {
                userAvatarRepository.findByUserIdAndIsActiveTrue(currentUser.getId())
                        .ifPresent(previousActive -> {
                            previousActive.setActive(false);
                            userAvatarRepository.save(previousActive);
                        });

                UserAvatar newAvatar = UserAvatar.builder()
                        .user(currentUser)
                        .storageKeyOriginal(originalKey)
                        .storageKeyMedium(mediumKey)
                        .storageKeyThumbnail(thumbnailKey)
                        .originalFilename(scannedFile.sanitizedFilename())
                        .contentType(scannedFile.detectedMimeType())
                        .fileSize(scannedFile.size())
                        .isActive(true)
                        .uploadedAt(LocalDateTime.now(clock))
                        .build();

                UserAvatar savedAvatar = userAvatarRepository.save(newAvatar);

                currentUser.setAvatarUrl(FILE_SERVING_PATH_PREFIX + thumbnailKey);
                userRepository.save(currentUser);

                return mapToResponseDTO(savedAvatar);
            });
        } catch (Exception ex) {
            log.error("Avatar upload pipeline failed for user {}. Initiating rollback compensation for {} stored keys: {}",
                    currentUser.getId(), storedKeys.size(), storedKeys, ex);
            compensateStorageDeletions(storedKeys);
            throw ex;
        }
    }

    /**
     * Executes compensating delete operations on storage keys that were persisted before a pipeline failure.
     *
     * @param keys list of storage keys to physically delete
     */
    private void compensateStorageDeletions(List<String> keys) {
        for (String key : keys) {
            try {
                storageService.delete(key);
                log.info("Rollback compensation: successfully deleted orphaned storage key '{}'", key);
            } catch (Exception e) {
                log.error("Rollback compensation failed to delete storage key '{}': {}", key, e.getMessage(), e);
            }
        }
    }

    /**
     * Maps a persisted UserAvatar entity into an AvatarResponseDTO.
     *
     * @param avatar the entity to convert
     * @return the populated response DTO
     */
    private AvatarResponseDTO mapToResponseDTO(UserAvatar avatar) {
        return AvatarResponseDTO.builder()
                .id(avatar.getId())
                .urlOriginal(FILE_SERVING_PATH_PREFIX + avatar.getStorageKeyOriginal())
                .urlMedium(FILE_SERVING_PATH_PREFIX + avatar.getStorageKeyMedium())
                .urlThumbnail(FILE_SERVING_PATH_PREFIX + avatar.getStorageKeyThumbnail())
                .originalFilename(avatar.getOriginalFilename())
                .contentType(avatar.getContentType())
                .fileSize(avatar.getFileSize())
                .isActive(avatar.isActive())
                .uploadedAt(avatar.getUploadedAt())
                .build();
    }
}
