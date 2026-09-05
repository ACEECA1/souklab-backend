package com.project.souklab.controller.user;

import com.project.souklab.controller.support.ControllerSliceTest;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.dto.user.AvatarResponseDTO;
import com.project.souklab.exception.AvatarLimitExceededException;
import com.project.souklab.filestorage.config.StorageProperties;
import com.project.souklab.filestorage.exception.UnsupportedFileTypeException;
import com.project.souklab.filestorage.exception.VirusDetectedException;
import com.project.souklab.model.User;
import com.project.souklab.service.user.AvatarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.unit.DataSize;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.project.souklab.controller.support.SecurityTestUtils.client;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller slice test for {@link AvatarController}.
 * Verifies routing, authentication enforcement, input validation, quota error mapping,
 * and HTTP response envelopes for POST /api/v1/users/me/avatars.
 */
@ControllerSliceTest(controllers = AvatarController.class)
class AvatarControllerTest {

    private static final String AVATAR_URL = "/api/v1/users/me/avatars";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AvatarService avatarService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private StorageProperties storageProperties;

    private User testUser;
    private MockMultipartFile validFile;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("client@souklab.com")
                .firstName("Amina")
                .lastName("K")
                .build();
        testUser.setId("user-client-123");

        validFile = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                new byte[]{1, 2, 3, 4}
        );

        StorageProperties.ValidationProperties validation = new StorageProperties.ValidationProperties();
        validation.setMaxFileSize(DataSize.ofMegabytes(10));
        when(storageProperties.getValidation()).thenReturn(validation);
    }

    @Nested
    @DisplayName("Authentication & Authorization")
    class SecurityTests {

        /**
         * Verifies unauthenticated calls receive 401 Unauthorized.
         */
        @Test
        @DisplayName("POST /api/v1/users/me/avatars without authentication returns 401 Unauthorized")
        void uploadAvatar_whenUnauthenticated_returns401() throws Exception {
            mockMvc.perform(multipart(AVATAR_URL)
                            .file(validFile))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false));

            verifyNoInteractions(avatarService);
        }
    }

    @Nested
    @DisplayName("Upload Success & Pipeline Mapping")
    class SuccessTests {

        /**
         * Verifies 201 Created response envelope with AvatarResponseDTO containing URLs for all 3 tiers.
         */
        @Test
        @DisplayName("POST /api/v1/users/me/avatars with valid file returns 201 Created and tier URLs")
        void uploadAvatar_whenValidFile_returns201Created() throws Exception {
            when(userRepository.findByEmail("client@souklab.com")).thenReturn(Optional.of(testUser));

            AvatarResponseDTO responseDTO = AvatarResponseDTO.builder()
                    .id("avatar-uuid-1")
                    .urlOriginal("/api/v1/files/orig-key.png")
                    .urlMedium("/api/v1/files/med-key.png")
                    .urlThumbnail("/api/v1/files/thumb-key.png")
                    .originalFilename("avatar.png")
                    .contentType("image/png")
                    .fileSize(4L)
                    .isActive(true)
                    .uploadedAt(LocalDateTime.now())
                    .build();

            when(avatarService.uploadAvatar(any(User.class), any())).thenReturn(responseDTO);

            mockMvc.perform(multipart(AVATAR_URL)
                            .file(validFile)
                            .with(client()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value("avatar-uuid-1"))
                    .andExpect(jsonPath("$.data.urlOriginal").value("/api/v1/files/orig-key.png"))
                    .andExpect(jsonPath("$.data.urlMedium").value("/api/v1/files/med-key.png"))
                    .andExpect(jsonPath("$.data.urlThumbnail").value("/api/v1/files/thumb-key.png"))
                    .andExpect(jsonPath("$.data.isActive").value(true));
        }
    }

    @Nested
    @DisplayName("Validation & Error Envelopes")
    class ValidationTests {

        /**
         * Verifies 400 Bad Request when the file parameter is empty.
         */
        @Test
        @DisplayName("POST /api/v1/users/me/avatars with empty file returns 400 Bad Request")
        void uploadAvatar_whenEmptyFile_returns400() throws Exception {
            MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

            mockMvc.perform(multipart(AVATAR_URL)
                            .file(emptyFile)
                            .with(client()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));

            verifyNoInteractions(avatarService);
        }

        /**
         * Verifies 400 Bad Request when file size exceeds configured avatar limit.
         */
        @Test
        @DisplayName("POST /api/v1/users/me/avatars with oversized file returns 400 Bad Request with FILE_TOO_LARGE")
        void uploadAvatar_whenOversizedFile_returns400() throws Exception {
            MockMultipartFile oversized = new MockMultipartFile(
                    "file",
                    "huge.jpg",
                    "image/jpeg",
                    new byte[1024 * 1024 * 11]
            );

            mockMvc.perform(multipart(AVATAR_URL)
                            .file(oversized)
                            .with(client()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("FILE_TOO_LARGE"));

            verifyNoInteractions(avatarService);
        }

        /**
         * Verifies 400 Bad Request when an unsupported file type is uploaded.
         */
        @Test
        @DisplayName("POST /api/v1/users/me/avatars with unsupported file type returns 400 Bad Request")
        void uploadAvatar_whenUnsupportedFileType_returns400() throws Exception {
            when(userRepository.findByEmail("client@souklab.com")).thenReturn(Optional.of(testUser));
            when(avatarService.uploadAvatar(any(), any()))
                    .thenThrow(new UnsupportedFileTypeException("application/pdf"));

            mockMvc.perform(multipart(AVATAR_URL)
                            .file(validFile)
                            .with(client()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("UNSUPPORTED_FILE_TYPE"));
        }

        /**
         * Verifies 422 Unprocessable Content when malware is detected.
         */
        @Test
        @DisplayName("POST /api/v1/users/me/avatars when virus detected returns 422 Unprocessable Content")
        void uploadAvatar_whenVirusDetected_returns422() throws Exception {
            when(userRepository.findByEmail("client@souklab.com")).thenReturn(Optional.of(testUser));
            when(avatarService.uploadAvatar(any(), any()))
                    .thenThrow(new VirusDetectedException("Eicar-Signature"));

            mockMvc.perform(multipart(AVATAR_URL)
                            .file(validFile)
                            .with(client()))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("VIRUS_DETECTED"));
        }

        /**
         * Verifies 409 Conflict when the user has already reached their 10-avatar gallery limit.
         */
        @Test
        @DisplayName("POST /api/v1/users/me/avatars when quota exceeded returns 409 Conflict")
        void uploadAvatar_whenQuotaExceeded_returns409() throws Exception {
            when(userRepository.findByEmail("client@souklab.com")).thenReturn(Optional.of(testUser));
            when(avatarService.uploadAvatar(any(), any()))
                    .thenThrow(new AvatarLimitExceededException("Maximum avatar limit of 10 reached"));

            mockMvc.perform(multipart(AVATAR_URL)
                            .file(validFile)
                            .with(client()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorCode").value("AVATAR_LIMIT_EXCEEDED"));
        }
    }
}
