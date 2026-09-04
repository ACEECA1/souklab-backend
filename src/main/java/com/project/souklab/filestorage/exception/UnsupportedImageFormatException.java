package com.project.souklab.filestorage.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an image processing operation is attempted on an unsupported or non-image content type,
 * or when image decoding fails.
 */
public class UnsupportedImageFormatException extends StorageException {

    private static final String DEFAULT_ERROR_CODE = "UNSUPPORTED_IMAGE_FORMAT";

    /**
     * Constructs an exception for an unsupported image MIME type.
     *
     * @param contentType the unsupported content type
     */
    public UnsupportedImageFormatException(String contentType) {
        super(HttpStatus.BAD_REQUEST, DEFAULT_ERROR_CODE,
                String.format("Content type '%s' is not supported for image processing", contentType));
    }

    /**
     * Constructs an exception with a custom message and cause.
     *
     * @param message failure detail message
     * @param cause underlying cause
     */
    public UnsupportedImageFormatException(String message, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, DEFAULT_ERROR_CODE, message, cause);
    }
}
