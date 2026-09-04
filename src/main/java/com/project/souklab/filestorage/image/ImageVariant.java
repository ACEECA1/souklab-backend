package com.project.souklab.filestorage.image;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Objects;

/**
 * Encapsulates a processed image variant, its assigned resolution tier, dimensions, and raw bytes.
 *
 * @param tier the resolution tier for this variant
 * @param bytes processed image payload bytes
 * @param width measured image width in pixels
 * @param height measured image height in pixels
 * @param contentType MIME content type (e.g. image/jpeg, image/png)
 */
public record ImageVariant(
        ResolutionTier tier,
        byte[] bytes,
        int width,
        int height,
        String contentType
) {
    public ImageVariant {
        Objects.requireNonNull(tier, "ResolutionTier cannot be null");
        Objects.requireNonNull(bytes, "Image bytes cannot be null");
        Objects.requireNonNull(contentType, "Content-Type cannot be null");
        if (width <= 0) {
            throw new IllegalArgumentException("Image width must be strictly positive, got: " + width);
        }
        if (height <= 0) {
            throw new IllegalArgumentException("Image height must be strictly positive, got: " + height);
        }
    }

    /**
     * Opens a new input stream positioned at index 0 over the in-memory variant bytes.
     *
     * @return a ByteArrayInputStream over the variant bytes
     */
    public InputStream getInputStream() {
        return new ByteArrayInputStream(bytes);
    }

    /**
     * Returns the byte length of the variant payload.
     *
     * @return payload size in bytes
     */
    public long getSize() {
        return bytes.length;
    }
}
