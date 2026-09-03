package com.project.souklab.filestorage;

import java.io.InputStream;

/**
 * Resource representation containing the readable file stream and associated metadata.
 *
 * @param key unique storage key
 * @param content readable stream containing file data
 * @param contentType MIME type of the stored file
 * @param size size in bytes
 * @param originalFilename original filename associated with the stored file
 */
public record StorageResource(
        String key,
        InputStream content,
        String contentType,
        long size,
        String originalFilename
) {}
