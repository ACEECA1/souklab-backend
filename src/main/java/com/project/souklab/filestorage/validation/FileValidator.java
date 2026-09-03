package com.project.souklab.filestorage.validation;

import com.project.souklab.filestorage.config.StorageProperties;
import com.project.souklab.filestorage.exception.FileTooLargeException;
import com.project.souklab.filestorage.exception.InvalidFilenameException;
import com.project.souklab.filestorage.exception.UnsupportedFileTypeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Provider-agnostic file validator and sanitizer component.
 * Executes size checking, magic-byte MIME type sniffing (via Apache Tika),
 * Content-Type spoof detection, and strict filename sanitization.
 *
 * <p><strong>Stream Buffering & Memory Strategy:</strong>
 * Uses a fixed 16KB bounded mark/reset buffer (via {@link BufferedInputStream}) for Tika magic-byte sniffing.
 * The entire stream is NEVER loaded into memory during validation, keeping heap consumption O(1) (~16KB)
 * even when validating multi-megabyte/gigabyte files for future S3/MinIO streaming.
 * In addition, the stream is wrapped in a {@link SizeLimitingInputStream} hard-capped at {@code maxFileSize} bytes,
 * preventing memory or disk exhaustion if a client declares a small size but sends a larger payload.
 *
 * <p><strong>Stream Consumption & Invariant Rule:</strong>
 * Any {@link com.project.souklab.filestorage.StorageService} implementation MUST consume the
 * {@link ValidatedFile#content()} wrapped stream (the stream that was already wrapped in
 * {@link SizeLimitingInputStream}) and MUST NEVER re-wrap or substitute the original raw stream.
 * The hard size-cap and memory-exhaustion defenses strictly depend on this invariant being respected
 * by every storage backend implementation (including the in-memory stub and future S3/MinIO backends in D.0b).
 *
 * <p><strong>CRITICAL ARCHITECTURAL RULE:</strong>
 * The sanitized filename produced by this component must NEVER be used as a literal storage path segment.
 * Storage keys must always be generated safe identifiers (e.g. UUID-based) to guarantee path isolation.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FileValidator {

    private static final int DETECTION_BUFFER_SIZE = 8192;

    private final StorageProperties properties;
    private final Tika tika;

    /**
     * Validates and sanitizes a byte array file upload.
     */
    public ValidatedFile validateAndSanitize(byte[] bytes, String originalFilename, String declaredContentType) {
        if (bytes == null || bytes.length == 0) {
            throw new UnsupportedFileTypeException("Uploaded file content cannot be empty");
        }
        return validateAndSanitize(new ByteArrayInputStream(bytes), originalFilename, declaredContentType, bytes.length);
    }

    /**
     * Validates and sanitizes an InputStream file upload.
     * Hard-caps stream reading to {@code maxFileSize} bytes regardless of declared size.
     */
    public ValidatedFile validateAndSanitize(InputStream content, String originalFilename, String declaredContentType, long declaredSize) {
        if (content == null) {
            throw new UnsupportedFileTypeException("Uploaded file stream cannot be null");
        }

        // 1. Initial size validation against declared size
        long maxBytes = properties.getValidation().getMaxFileSize().toBytes();
        if (declaredSize > maxBytes) {
            log.warn("File upload rejected: declared size {} bytes exceeds maximum limit {} bytes", declaredSize, maxBytes);
            throw new FileTooLargeException(declaredSize, maxBytes);
        }

        // 2. Wrap in SizeLimitingInputStream to enforce hard-cap during stream consumption
        SizeLimitingInputStream boundedStream = new SizeLimitingInputStream(content, maxBytes);

        // 3. Filename sanitization
        String sanitizedFilename = sanitizeFilename(originalFilename);

        // 4. Prepare markable stream for magic-byte sniffing (fixed 16KB bounded buffer)
        InputStream stream = boundedStream.markSupported()
                ? boundedStream
                : new BufferedInputStream(boundedStream, DETECTION_BUFFER_SIZE * 2);
        stream.mark(DETECTION_BUFFER_SIZE * 2);

        // 5. Content-based MIME detection via Tika (magic bytes)
        String detectedMimeType;
        try {
            detectedMimeType = tika.detect(stream, sanitizedFilename);
            stream.reset();
        } catch (IOException e) {
            log.error("Failed to sniff file MIME type from input stream", e);
            throw new UnsupportedFileTypeException("Could not determine file type from content signature", e);
        }

        // 6. Allowed MIME types validation
        List<String> allowedTypes = properties.getValidation().getAllowedMimeTypes();
        boolean isAllowed = allowedTypes.stream()
                .anyMatch(allowed -> allowed.equalsIgnoreCase(detectedMimeType));

        if (!isAllowed) {
            log.warn("File upload rejected: sniffed MIME type '{}' is not in allowed list {}", detectedMimeType, allowedTypes);
            throw new UnsupportedFileTypeException(detectedMimeType);
        }

        // 7. Anti-spoofing check: compare detected MIME against declared Content-Type
        if (declaredContentType != null && !declaredContentType.isBlank()) {
            String cleanDeclared = declaredContentType.split(";")[0].trim().toLowerCase();
            if (!isMatchingMimeType(cleanDeclared, detectedMimeType)) {
                log.warn("MIME spoofing detected! Sniffed magic bytes: '{}', Declared header: '{}'", detectedMimeType, declaredContentType);
                throw new UnsupportedFileTypeException(detectedMimeType, declaredContentType);
            }
        }

        return new ValidatedFile(stream, sanitizedFilename, detectedMimeType, declaredSize);
    }

    /**
     * Sanitizes an original filename to remove path traversal sequences, control characters,
     * and non-whitelisted symbols.
     */
    public String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new InvalidFilenameException("Filename must not be null or blank");
        }

        if (filename.contains("\0")) {
            throw new InvalidFilenameException("Filename contains illegal null byte character");
        }

        // Strip directory prefixes (both Unix / and Windows \)
        String clean = filename.replace('\\', '/');
        int lastSlash = clean.lastIndexOf('/');
        if (lastSlash >= 0) {
            clean = clean.substring(lastSlash + 1);
        }

        // Strip path traversal sequences
        clean = clean.replace("..", "");

        // Whitelist safe characters: alphanumeric, dot, underscore, dash
        clean = clean.replaceAll("[^a-zA-Z0-9._-]", "_").trim();

        // Reject dangerous / empty outcomes
        if (clean.isBlank() || clean.equals(".") || clean.equals("_")) {
            throw new InvalidFilenameException("Filename is invalid or dangerous: " + filename);
        }

        return clean;
    }

    private boolean isMatchingMimeType(String declared, String detected) {
        if (declared.equalsIgnoreCase(detected)) {
            return true;
        }
        // Handle common equivalent MIME aliases
        if ((declared.equals("image/jpg") && detected.equalsIgnoreCase("image/jpeg")) ||
            (declared.equals("image/jpeg") && detected.equalsIgnoreCase("image/jpg"))) {
            return true;
        }
        return false;
    }
}
