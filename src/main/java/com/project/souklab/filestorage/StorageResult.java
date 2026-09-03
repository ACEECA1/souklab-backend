package com.project.souklab.filestorage;

import java.time.Instant;

/**
 * Immutable metadata result returned after a file is successfully stored.
 *
 * @param key unique storage key generated for the file (never the original filename)
 * @param originalFilename sanitized original filename preserved as metadata
 * @param contentType verified MIME type of the stored file
 * @param size size in bytes
 * @param storedAt timestamp when the file was persisted
 */
public record StorageResult(
        String key,
        String originalFilename,
        String contentType,
        long size,
        Instant storedAt
) {}
