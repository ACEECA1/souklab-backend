package com.project.souklab.filestorage.validation;

import java.io.InputStream;

/**
 * Encapsulates a validated file stream, its verified metadata, and sanitized original filename.
 *
 * @param content readable stream positioned at the start, ready for backend storage
 * @param sanitizedFilename clean, safe filename preserved ONLY as metadata (NEVER used as storage path)
 * @param detectedMimeType verified MIME type detected via magic-byte content sniffing
 * @param size verified file size in bytes
 */
public record ValidatedFile(
        InputStream content,
        String sanitizedFilename,
        String detectedMimeType,
        long size
) {}
