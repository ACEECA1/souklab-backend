package com.project.souklab.service.user;

import com.project.souklab.dao.UserAvatarRepository;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.dto.user.AvatarResponseDTO;
import com.project.souklab.exception.AvatarLimitExceededException;
import com.project.souklab.exception.BadRequestException;
import com.project.souklab.filestorage.StorageResult;
import com.project.souklab.filestorage.StorageService;
import com.project.souklab.filestorage.exception.StorageException;
import com.project.souklab.filestorage.exception.VirusDetectedException;
import com.project.souklab.filestorage.image.ImageProcessingService;
import com.project.souklab.filestorage.image.ImageVariant;
import com.project.souklab.filestorage.image.ResolutionTier;
import com.project.souklab.filestorage.scan.VirusScanService;
import com.project.souklab.filestorage.validation.FileValidator;
import com.project.souklab.filestorage.validation.ValidatedFile;
import com.project.souklab.model.User;
import com.project.souklab.model.UserAvatar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AvatarService} verifying quota enforcement, pipeline sequencing,
 * multi-tier storage coordination, transactional profile synchronization, and compensating rollback deletions.
 */
@ExtendWith(MockitoExtension.class)
class AvatarServiceTest {

    @Mock
    private UserAvatarRepository userAvatarRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileValidator fileValidator;

    @Mock
    private VirusScanService virusScanService;

    @Mock
    private ImageProcessingService imageProcessingService;

    @Mock
    private StorageService storageService;

    @Mock
    private TransactionTemplate transactionTemplate;

    private Clock clock;
    private AvatarService avatarService;
    private User testUser;
    private MockMultipartFile validFile;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-09-05T12:00:00Z"), ZoneId.of("UTC"));
        avatarService = new AvatarService(
                userAvatarRepository,
                userRepository,
                fileValidator,
                virusScanService,
                imageProcessingService,
                storageService,
                transactionTemplate,
                clock
        );

        testUser = User.builder()
                .email("artisan@souklab.dz")
                .firstName("Karim")
                .lastName("B")
                .avatarUrl("/api/v1/files/old-avatar.jpg")
                .build();
        testUser.setId("user-uuid-123");

        validFile = new MockMultipartFile(
                "file",
                "portrait.png",
                "image/png",
                new byte[]{1, 2, 3, 4}
        );
    }

    /**
     * Helper to mock TransactionTemplate.execute by invoking the callback synchronously.
     */
    @SuppressWarnings("unchecked")
    private void mockTransactionTemplateSuccess() {
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction((TransactionStatus) null);
        });
    }

    /**
     * Helper generating mock ImageVariants across all three tiers.
     */
    private Map<ResolutionTier, ImageVariant> createMockVariants() {
        Map<ResolutionTier, ImageVariant> map = new EnumMap<>(ResolutionTier.class);
        map.put(ResolutionTier.ORIGINAL, new ImageVariant(ResolutionTier.ORIGINAL, new byte[]{1, 1}, 1000, 1000, "image/png"));
        map.put(ResolutionTier.MEDIUM, new ImageVariant(ResolutionTier.MEDIUM, new byte[]{2, 2}, 500, 500, "image/png"));
        map.put(ResolutionTier.THUMBNAIL, new ImageVariant(ResolutionTier.THUMBNAIL, new byte[]{3, 3}, 150, 150, "image/png"));
        return map;
    }

    /**
     * Verifies the full happy path pipeline: quota check, validation, scanning, variant generation,
     * storage of all 3 tiers, prior active avatar deactivation, new avatar persistence, and User.avatarUrl update.
     */
    @Test
    @DisplayName("uploadAvatar succeeds and executes full pipeline in strict sequence")
    void uploadAvatar_whenValidInput_orchestratesFullPipelineAndActivatesAvatar() throws Exception {
        when(userAvatarRepository.countByUserId(testUser.getId())).thenReturn(2L);

        ValidatedFile validatedFile = new ValidatedFile(new ByteArrayInputStream(validFile.getBytes()), "portrait.png", "image/png", validFile.getSize());
        when(fileValidator.validateAndSanitize(any(InputStream.class), eq("portrait.png"), eq("image/png"), eq(validFile.getSize()), eq(AvatarService.AVATAR_ALLOWED_MIME_TYPES)))
                .thenReturn(validatedFile);

        ValidatedFile scannedFile = new ValidatedFile(new ByteArrayInputStream(validFile.getBytes()), "portrait.png", "image/png", validFile.getSize());
        when(virusScanService.scan(validatedFile)).thenReturn(scannedFile);

        when(imageProcessingService.generateVariants(scannedFile)).thenReturn(createMockVariants());

        StorageResult origResult = new StorageResult("key-orig-123.png", "portrait.png", "image/png", 2L, Instant.now(clock));
        StorageResult medResult = new StorageResult("key-med-123.png", "portrait.png", "image/png", 2L, Instant.now(clock));
        StorageResult thumbResult = new StorageResult("key-thumb-123.png", "portrait.png", "image/png", 2L, Instant.now(clock));
        when(storageService.store(any(InputStream.class), eq("portrait.png"), eq("image/png"), anyLong()))
                .thenReturn(origResult, medResult, thumbResult);

        UserAvatar priorActive = UserAvatar.builder()
                .user(testUser)
                .isActive(true)
                .storageKeyThumbnail("old-thumb.png")
                .build();
        priorActive.setId("old-avatar-id");
        when(userAvatarRepository.findByUserIdAndIsActiveTrue(testUser.getId())).thenReturn(Optional.of(priorActive));

        when(userAvatarRepository.save(any(UserAvatar.class))).thenAnswer(invocation -> {
            UserAvatar a = invocation.getArgument(0);
            if (a.getId() == null) {
                a.setId("new-avatar-id");
            }
            return a;
        });

        mockTransactionTemplateSuccess();

        AvatarResponseDTO response = avatarService.uploadAvatar(testUser, validFile);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("new-avatar-id");
        assertThat(response.getUrlOriginal()).isEqualTo("/api/v1/files/key-orig-123.png");
        assertThat(response.getUrlMedium()).isEqualTo("/api/v1/files/key-med-123.png");
        assertThat(response.getUrlThumbnail()).isEqualTo("/api/v1/files/key-thumb-123.png");
        assertThat(response.isActive()).isTrue();
        assertThat(response.getUploadedAt()).isEqualTo(LocalDateTime.now(clock));

        assertThat(priorActive.isActive()).isFalse();
        assertThat(testUser.getAvatarUrl()).isEqualTo("/api/v1/files/key-thumb-123.png");
        verify(userRepository).save(testUser);

        InOrder inOrder = inOrder(userAvatarRepository, fileValidator, virusScanService, imageProcessingService, storageService, transactionTemplate);
        inOrder.verify(userAvatarRepository).countByUserId(testUser.getId());
        inOrder.verify(fileValidator).validateAndSanitize(any(), eq("portrait.png"), eq("image/png"), eq(validFile.getSize()), eq(AvatarService.AVATAR_ALLOWED_MIME_TYPES));
        inOrder.verify(virusScanService).scan(validatedFile);
        inOrder.verify(imageProcessingService).generateVariants(scannedFile);
        inOrder.verify(storageService, times(3)).store(any(), any(), any(), anyLong());
        inOrder.verify(transactionTemplate).execute(any());
    }

    /**
     * Verifies that when a user already has 10 avatars, upload is rejected with AvatarLimitExceededException
     * without calling any expensive downstream services.
     */
    @Test
    @DisplayName("uploadAvatar rejects upload with 409 AvatarLimitExceededException when count reaches 10")
    void uploadAvatar_whenQuotaExceeded_throwsAvatarLimitExceededExceptionWithoutCallingDownstreamServices() {
        when(userAvatarRepository.countByUserId(testUser.getId())).thenReturn(10L);

        assertThatThrownBy(() -> avatarService.uploadAvatar(testUser, validFile))
                .isInstanceOf(AvatarLimitExceededException.class)
                .hasMessageContaining("Maximum avatar limit of 10 reached");

        verifyNoInteractions(fileValidator);
        verifyNoInteractions(virusScanService);
        verifyNoInteractions(imageProcessingService);
        verifyNoInteractions(storageService);
        verifyNoInteractions(transactionTemplate);
    }

    /**
     * Verifies that when malware is detected, VirusDetectedException propagates and storageService is never invoked.
     */
    @Test
    @DisplayName("uploadAvatar propagates VirusDetectedException and never invokes StorageService")
    void uploadAvatar_whenVirusDetected_propagatesExceptionWithoutStoringFiles() throws Exception {
        when(userAvatarRepository.countByUserId(testUser.getId())).thenReturn(0L);

        ValidatedFile validatedFile = new ValidatedFile(new ByteArrayInputStream(validFile.getBytes()), "eicar.png", "image/png", validFile.getSize());
        when(fileValidator.validateAndSanitize(any(), any(), any(), anyLong(), any())).thenReturn(validatedFile);

        when(virusScanService.scan(validatedFile)).thenThrow(new VirusDetectedException("Eicar-Test-Signature"));

        assertThatThrownBy(() -> avatarService.uploadAvatar(testUser, validFile))
                .isInstanceOf(VirusDetectedException.class)
                .hasMessageContaining("Eicar-Test-Signature");

        verifyNoInteractions(imageProcessingService);
        verifyNoInteractions(storageService);
        verifyNoInteractions(transactionTemplate);
    }

    /**
     * Verifies rollback compensation when the second store call fails: the first stored key is deleted.
     */
    @Test
    @DisplayName("uploadAvatar triggers compensating delete for key 1 when store call 2 fails")
    void uploadAvatar_whenSecondStoreFails_executesCompensatingDeleteOnFirstKey() throws Exception {
        when(userAvatarRepository.countByUserId(testUser.getId())).thenReturn(0L);

        ValidatedFile validatedFile = new ValidatedFile(new ByteArrayInputStream(validFile.getBytes()), "test.png", "image/png", validFile.getSize());
        when(fileValidator.validateAndSanitize(any(), any(), any(), anyLong(), any())).thenReturn(validatedFile);
        when(virusScanService.scan(validatedFile)).thenReturn(validatedFile);
        when(imageProcessingService.generateVariants(validatedFile)).thenReturn(createMockVariants());

        when(storageService.store(any(), any(), any(), anyLong()))
                .thenReturn(new StorageResult("key-orig-only.png", "test.png", "image/png", 2L, Instant.now(clock)))
                .thenThrow(new StorageException("S3 connection timeout on medium tier"));

        assertThatThrownBy(() -> avatarService.uploadAvatar(testUser, validFile))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("S3 connection timeout on medium tier");

        verify(storageService).delete("key-orig-only.png");
        verify(storageService, never()).delete("key-med.png");
        verifyNoInteractions(transactionTemplate);
    }

    /**
     * Verifies rollback compensation when the third store call fails: both the first and second keys are deleted.
     */
    @Test
    @DisplayName("uploadAvatar triggers compensating delete for keys 1 and 2 when store call 3 fails")
    void uploadAvatar_whenThirdStoreFails_executesCompensatingDeleteOnFirstTwoKeys() throws Exception {
        when(userAvatarRepository.countByUserId(testUser.getId())).thenReturn(0L);

        ValidatedFile validatedFile = new ValidatedFile(new ByteArrayInputStream(validFile.getBytes()), "test.png", "image/png", validFile.getSize());
        when(fileValidator.validateAndSanitize(any(), any(), any(), anyLong(), any())).thenReturn(validatedFile);
        when(virusScanService.scan(validatedFile)).thenReturn(validatedFile);
        when(imageProcessingService.generateVariants(validatedFile)).thenReturn(createMockVariants());

        when(storageService.store(any(), any(), any(), anyLong()))
                .thenReturn(new StorageResult("key-orig.png", "test.png", "image/png", 2L, Instant.now(clock)))
                .thenReturn(new StorageResult("key-med.png", "test.png", "image/png", 2L, Instant.now(clock)))
                .thenThrow(new StorageException("Disk full on thumbnail tier"));

        assertThatThrownBy(() -> avatarService.uploadAvatar(testUser, validFile))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("Disk full on thumbnail tier");

        verify(storageService).delete("key-orig.png");
        verify(storageService).delete("key-med.png");
        verifyNoInteractions(transactionTemplate);
    }

    /**
     * Verifies rollback compensation when the database transaction fails after all 3 stores succeeded:
     * all 3 stored keys are deleted to prevent orphaned S3 objects.
     */
    @Test
    @DisplayName("uploadAvatar triggers compensating delete for all 3 keys when database transaction fails")
    void uploadAvatar_whenDatabaseTransactionFails_executesCompensatingDeleteOnAllThreeKeys() throws Exception {
        when(userAvatarRepository.countByUserId(testUser.getId())).thenReturn(0L);

        ValidatedFile validatedFile = new ValidatedFile(new ByteArrayInputStream(validFile.getBytes()), "test.png", "image/png", validFile.getSize());
        when(fileValidator.validateAndSanitize(any(), any(), any(), anyLong(), any())).thenReturn(validatedFile);
        when(virusScanService.scan(validatedFile)).thenReturn(validatedFile);
        when(imageProcessingService.generateVariants(validatedFile)).thenReturn(createMockVariants());

        when(storageService.store(any(), any(), any(), anyLong()))
                .thenReturn(new StorageResult("k1.png", "test.png", "image/png", 2L, Instant.now(clock)))
                .thenReturn(new StorageResult("k2.png", "test.png", "image/png", 2L, Instant.now(clock)))
                .thenReturn(new StorageResult("k3.png", "test.png", "image/png", 2L, Instant.now(clock)));

        when(transactionTemplate.execute(any())).thenThrow(new RuntimeException("Database deadlock"));

        assertThatThrownBy(() -> avatarService.uploadAvatar(testUser, validFile))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database deadlock");

        verify(storageService).delete("k1.png");
        verify(storageService).delete("k2.png");
        verify(storageService).delete("k3.png");
    }

    /**
     * Verifies that a missing or empty file throws BadRequestException immediately.
     */
    @Test
    @DisplayName("uploadAvatar rejects empty or null files with BadRequestException")
    void uploadAvatar_whenFileEmptyOrNull_throwsBadRequestException() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> avatarService.uploadAvatar(testUser, null))
                .isInstanceOf(BadRequestException.class);

        assertThatThrownBy(() -> avatarService.uploadAvatar(testUser, emptyFile))
                .isInstanceOf(BadRequestException.class);

        verifyNoInteractions(userAvatarRepository);
        verifyNoInteractions(fileValidator);
    }
}
