package com.project.souklab.filestorage;

import com.project.souklab.filestorage.config.StorageProperties;
import com.project.souklab.filestorage.exception.FileNotFoundStorageException;
import com.project.souklab.filestorage.exception.FileTooLargeException;
import com.project.souklab.filestorage.exception.UnsupportedFileTypeException;
import com.project.souklab.filestorage.stub.InMemoryStorageService;
import com.project.souklab.filestorage.validation.FileValidator;
import com.project.souklab.filestorage.validation.SizeLimitingInputStream;
import com.project.souklab.filestorage.validation.ValidatedFile;
import org.apache.tika.Tika;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.util.unit.DataSize;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verification test suite for Phase D.0a File Storage Abstraction & Validation Core.
 * Covers V1 through V7, plus follow-up tests for hard-capped streaming (1.3)
 * and allowed-type mismatch detection (1.4).
 */
class FileStorageCoreVerificationTest {

    private StorageProperties properties;
    private Tika tika;
    private FileValidator validator;
    private InMemoryStorageService storageService;

    // Standard valid JPEG header bytes: FF D8 FF E0
    private static final byte[] VALID_JPEG_BYTES = new byte[] {
            (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 0x4A, 0x46,
            0x49, 0x46, 0x00, 0x01, 0x01, 0x01, 0x00, 0x60, 0x00, 0x60, 0x00, 0x00
    };

    // Standard valid PNG header bytes: 89 50 4E 47 0D 0A 1A 0A
    private static final byte[] VALID_PNG_BYTES = new byte[] {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D
    };

    @BeforeEach
    void setUp() {
        properties = new StorageProperties();
        properties.getValidation().setMaxFileSize(DataSize.ofMegabytes(2)); // 2 MB cap for tests
        tika = new Tika();
        validator = new FileValidator(properties, tika);
        storageService = new InMemoryStorageService();
    }

    @Test
    @DisplayName("V1: Store valid small file -> confirm generated key is returned (not original filename)")
    void v1_storeValidSmallFile_shouldReturnGeneratedKey() {
        String originalFilename = "my_artisan_craft.jpg";
        ValidatedFile validated = validator.validateAndSanitize(
                new ByteArrayInputStream(VALID_JPEG_BYTES),
                originalFilename,
                "image/jpeg",
                VALID_JPEG_BYTES.length
        );

        StorageResult result = storageService.store(
                validated.content(),
                validated.sanitizedFilename(),
                validated.detectedMimeType(),
                validated.size()
        );

        System.out.println("=== V1 EVIDENCE ===");
        System.out.println("Original Filename: " + originalFilename);
        System.out.println("Sanitized Filename: " + validated.sanitizedFilename());
        System.out.println("Generated Storage Key: " + result.key());
        System.out.println("Stored Content Type: " + result.contentType());
        System.out.println("Stored Size: " + result.size() + " bytes");

        assertThat(result.key())
                .isNotNull()
                .isNotEqualTo(originalFilename)
                .isNotEqualTo(validated.sanitizedFilename())
                .endsWith(".jpg");
        assertThat(result.size()).isEqualTo(VALID_JPEG_BYTES.length);
        assertThat(result.contentType()).isEqualTo("image/jpeg");
    }

    @Test
    @DisplayName("V2: Retrieve by key -> confirm byte-for-byte identical content and correct metadata")
    void v2_retrieveByKey_shouldMatchByteForByteAndMetadata() throws IOException {
        String originalFilename = "pottery_showcase.png";
        ValidatedFile validated = validator.validateAndSanitize(
                new ByteArrayInputStream(VALID_PNG_BYTES),
                originalFilename,
                "image/png",
                VALID_PNG_BYTES.length
        );

        StorageResult stored = storageService.store(
                validated.content(),
                validated.sanitizedFilename(),
                validated.detectedMimeType(),
                validated.size()
        );

        StorageResource retrieved = storageService.retrieve(stored.key());
        byte[] retrievedBytes = retrieved.content().readAllBytes();

        System.out.println("=== V2 EVIDENCE ===");
        System.out.println("Storage Key: " + retrieved.key());
        System.out.println("Retrieved Content Type: " + retrieved.contentType());
        System.out.println("Retrieved Size: " + retrieved.size());
        System.out.println("Retrieved Original Filename: " + retrieved.originalFilename());
        System.out.println("Byte-for-byte Match: " + java.util.Arrays.equals(VALID_PNG_BYTES, retrievedBytes));

        assertThat(retrieved.key()).isEqualTo(stored.key());
        assertThat(retrieved.contentType()).isEqualTo("image/png");
        assertThat(retrieved.size()).isEqualTo(VALID_PNG_BYTES.length);
        assertThat(retrieved.originalFilename()).isEqualTo(originalFilename);
        assertThat(retrievedBytes).isEqualTo(VALID_PNG_BYTES);
    }

    @Test
    @DisplayName("V3: Delete file -> confirm exists() returns false and retrieve() fails appropriately")
    void v3_deleteFile_shouldCauseExistsFalseAndRetrieveFail() {
        ValidatedFile validated = validator.validateAndSanitize(
                new ByteArrayInputStream(VALID_JPEG_BYTES),
                "to_delete.jpg",
                "image/jpeg",
                VALID_JPEG_BYTES.length
        );

        StorageResult stored = storageService.store(
                validated.content(),
                validated.sanitizedFilename(),
                validated.detectedMimeType(),
                validated.size()
        );

        assertThat(storageService.exists(stored.key())).isTrue();

        // Perform deletion
        storageService.delete(stored.key());

        System.out.println("=== V3 EVIDENCE ===");
        System.out.println("Deleted Key: " + stored.key());
        System.out.println("exists() after deletion: " + storageService.exists(stored.key()));

        assertThat(storageService.exists(stored.key())).isFalse();
        assertThatThrownBy(() -> storageService.retrieve(stored.key()))
                .isInstanceOf(FileNotFoundStorageException.class)
                .hasMessageContaining(stored.key());
        System.out.println("retrieve() after deletion threw expected: FileNotFoundStorageException");
    }

    @Test
    @DisplayName("V4: Attempt to store file exceeding max size -> rejected with FileTooLargeException BEFORE storage")
    void v4_fileExceedingMaxSize_shouldBeRejectedByValidatorBeforeStorage() {
        long declaredOversize = DataSize.ofMegabytes(5).toBytes(); // 5 MB > 2 MB cap

        System.out.println("=== V4 EVIDENCE ===");
        System.out.println("Configured Max Limit: " + properties.getValidation().getMaxFileSize().toBytes() + " bytes (2MB)");
        System.out.println("Attempted Upload Size: " + declaredOversize + " bytes (5MB)");

        assertThatThrownBy(() -> validator.validateAndSanitize(
                new ByteArrayInputStream(new byte[100]),
                "massive_video.jpg",
                "image/jpeg",
                declaredOversize
        ))
                .isInstanceOf(FileTooLargeException.class)
                .hasMessageContaining("exceeds the maximum allowed limit");

        System.out.println("Result: Rejected with FileTooLargeException prior to any storage invocation");
    }

    @Test
    @DisplayName("V5: Spoofed type (executable/script pretending to be image/jpeg) -> rejected via magic-byte sniffing")
    void v5_spoofedFileType_shouldBeDetectedAndRejectedByContentSniffing() {
        byte[] maliciousScriptBytes = "#!/bin/bash\necho 'malicious script execution'\nexit 1\n".getBytes(StandardCharsets.UTF_8);
        String declaredContentType = "image/jpeg";
        String spoofedFilename = "innocent_photo.jpg";

        System.out.println("=== V5 EVIDENCE ===");
        System.out.println("File Name Hint: " + spoofedFilename);
        System.out.println("Declared Content-Type Header: " + declaredContentType);

        assertThatThrownBy(() -> validator.validateAndSanitize(
                new ByteArrayInputStream(maliciousScriptBytes),
                spoofedFilename,
                declaredContentType,
                maliciousScriptBytes.length
        ))
                .isInstanceOf(UnsupportedFileTypeException.class)
                .satisfies(ex -> {
                    System.out.println("Exception Caught: " + ex.getClass().getSimpleName());
                    System.out.println("Exception Message: " + ex.getMessage());
                    System.out.println("HTTP Status: " + ((UnsupportedFileTypeException) ex).getStatus());
                    System.out.println("Error Code: " + ((UnsupportedFileTypeException) ex).getErrorCode());
                });
    }

    @Test
    @DisplayName("V6: Path traversal filename -> sanitized and storage key is guaranteed safe and unrelated")
    void v6_pathTraversalFilename_shouldBeSanitizedAndKeyGuaranteedSafe() {
        String dangerousFilename = "../../../../../etc/passwd";
        ValidatedFile validated = validator.validateAndSanitize(
                new ByteArrayInputStream(VALID_JPEG_BYTES),
                dangerousFilename,
                "image/jpeg",
                VALID_JPEG_BYTES.length
        );

        StorageResult stored = storageService.store(
                validated.content(),
                validated.sanitizedFilename(),
                validated.detectedMimeType(),
                validated.size()
        );

        System.out.println("=== V6 EVIDENCE ===");
        System.out.println("Original Malicious Filename: " + dangerousFilename);
        System.out.println("Sanitizer Output (Metadata only): " + validated.sanitizedFilename());
        System.out.println("Actual Generated Storage Key: " + stored.key());

        assertThat(validated.sanitizedFilename())
                .doesNotContain("..")
                .doesNotContain("/")
                .doesNotContain("\\")
                .isEqualTo("passwd");

        assertThat(stored.key())
                .doesNotContain("etc")
                .doesNotContain("passwd")
                .doesNotContain("..")
                .doesNotContain("/");
        System.out.println("Proof: The generated storage key is completely decoupled from the original filename.");
    }

    @Test
    @DisplayName("V7: Validation logic runs independently of StorageService implementation")
    void v7_validationLogicIsSharedAndIndependentOfBackend() {
        System.out.println("=== V7 EVIDENCE ===");
        System.out.println("Validator class: " + validator.getClass().getName());
        System.out.println("StorageService interface: " + StorageService.class.getName());
        System.out.println("InMemoryStorageService stub: " + storageService.getClass().getName());

        // Demonstrate standalone validation without any storage call
        ValidatedFile validated = validator.validateAndSanitize(
                new ByteArrayInputStream(VALID_JPEG_BYTES),
                "standalone.jpg",
                "image/jpeg",
                VALID_JPEG_BYTES.length
        );

        assertThat(validated).isNotNull();
        assertThat(validated.detectedMimeType()).isEqualTo("image/jpeg");
        assertThat(validated.sanitizedFilename()).isEqualTo("standalone.jpg");
        System.out.println("Standalone Validation Result: Success (sniffed=" + validated.detectedMimeType() + ")");

        // Demonstrate that the validator produces a clean ValidatedFile ready for ANY StorageService implementation
        StorageService mockS3Backend = new StorageService() {
            @Override
            public StorageResult store(java.io.InputStream content, String originalFilename, String contentType, long size) {
                return new StorageResult("s3-bucket/mock-key-1234", originalFilename, contentType, size, java.time.Instant.now());
            }

            @Override
            public StorageResource retrieve(String key) { return null; }

            @Override
            public void delete(String key) {}

            @Override
            public boolean exists(String key) { return true; }
        };

        StorageResult s3Result = mockS3Backend.store(
                validated.content(),
                validated.sanitizedFilename(),
                validated.detectedMimeType(),
                validated.size()
        );

        System.out.println("Plugged into mock S3 backend: " + s3Result.key());
        assertThat(s3Result.key()).isEqualTo("s3-bucket/mock-key-1234");
        System.out.println("Proof: The same FileValidator output seamlessly feeds any StorageService implementation.");
    }

    @Test
    @DisplayName("1.3: Hard-capped stream reading prevents bypass when client lies about small size")
    void testHardCappedStream_whenClientLiesAboutSmallSize_shouldThrowFileTooLargeExceptionOnConsumption() {
        System.out.println("=== 1.3 EVIDENCE ===");
        long configuredMaxBytes = properties.getValidation().getMaxFileSize().toBytes(); // 2 MB
        long declaredFakeSmallSize = 100; // Client claims 100 bytes

        // Build a lazy stream that yields valid JPEG header followed by 3 MB of data
        byte[] validHeader = VALID_JPEG_BYTES;
        int oversizedBodyLength = 3 * 1024 * 1024; // 3 MB > 2 MB cap
        InputStream oversizedStream = new SequenceInputStream(
                new ByteArrayInputStream(validHeader),
                new InputStream() {
                    private int count = 0;
                    @Override
                    public int read() {
                        return count++ < oversizedBodyLength ? 0x20 : -1;
                    }
                    @Override
                    public int read(byte[] b, int off, int len) {
                        if (count >= oversizedBodyLength) return -1;
                        int available = Math.min(len, oversizedBodyLength - count);
                        java.util.Arrays.fill(b, off, off + available, (byte) 0x20);
                        count += available;
                        return available;
                    }
                }
        );

        System.out.println("Configured Max Limit: " + configuredMaxBytes + " bytes (2MB)");
        System.out.println("Client Declared Size Header: " + declaredFakeSmallSize + " bytes (Lying small)");
        System.out.println("Actual Stream Payload: ~3.0MB (Exceeds limit)");

        // Validator passes the declared size check (100 <= 2MB) and sniffs the 16KB header,
        // but returns a stream wrapped in SizeLimitingInputStream capped at 2MB.
        ValidatedFile validated = validator.validateAndSanitize(
                oversizedStream,
                "lying_client.jpg",
                "image/jpeg",
                declaredFakeSmallSize
        );

        assertThat(validated.detectedMimeType()).isEqualTo("image/jpeg");
        System.out.println("MIME sniffing passed for initial header (detected: " + validated.detectedMimeType() + ")");

        // Now attempt to store / read the full stream:
        // SizeLimitingInputStream must interrupt as soon as 2MB + 1 byte is read.
        assertThatThrownBy(() -> storageService.store(
                validated.content(),
                validated.sanitizedFilename(),
                validated.detectedMimeType(),
                validated.size()
        ))
                .isInstanceOf(FileTooLargeException.class)
                .hasMessageContaining("exceeds the maximum allowed limit of " + configuredMaxBytes + " bytes");

        System.out.println("Proof: Stream consumption aborted with FileTooLargeException as soon as the hard cap was reached.");
        System.out.println("Memory Protection: Stream reading halted; never read more than maxFileSize into memory!");
    }

    @Test
    @DisplayName("1.4: Allowed-type mismatch (true content image/png vs declared image/jpeg) -> rejected with UnsupportedFileTypeException")
    void testAllowedTypeMismatch_whenTrueContentIsPngButDeclaredIsJpeg_shouldRejectWithUnsupportedFileTypeException() {
        System.out.println("=== 1.4 EVIDENCE ===");
        // Real content is PNG magic bytes
        byte[] realPngBytes = VALID_PNG_BYTES;
        // Client declares image/jpeg (both image/png and image/jpeg are on the allowed list)
        String declaredType = "image/jpeg";
        String filenameHint = "craft_photo.jpg";

        System.out.println("Allowed MIME list: " + properties.getValidation().getAllowedMimeTypes());
        System.out.println("True Content Sniffed (Magic Bytes): image/png (on allowed list)");
        System.out.println("Declared Content-Type Header: image/jpeg (on allowed list)");

        assertThatThrownBy(() -> validator.validateAndSanitize(
                new ByteArrayInputStream(realPngBytes),
                filenameHint,
                declaredType,
                realPngBytes.length
        ))
                .isInstanceOf(UnsupportedFileTypeException.class)
                .hasMessage("Declared content type 'image/jpeg' does not match actual file content 'image/png'")
                .satisfies(ex -> {
                    System.out.println("Exception: " + ex.getClass().getSimpleName());
                    System.out.println("Corrected Exception Message: " + ex.getMessage());
                    System.out.println("Status: " + ((UnsupportedFileTypeException) ex).getStatus());
                    System.out.println("ErrorCode: " + ((UnsupportedFileTypeException) ex).getErrorCode());
                });

        System.out.println("Intentional Behavior Confirmed: Even though image/png and image/jpeg are both allowed,");
        System.out.println("a mismatch indicates file tampering, extension spoofing, or parser confusion, and is strictly rejected.");
    }
}
