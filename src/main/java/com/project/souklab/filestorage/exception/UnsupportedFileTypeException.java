package com.project.souklab.filestorage.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an uploaded file fails MIME validation (either disallowed type or content spoofing).
 */
public class UnsupportedFileTypeException extends StorageException {

    private static final String DEFAULT_ERROR_CODE = "UNSUPPORTED_FILE_TYPE";

    public UnsupportedFileTypeException(String detectedMimeType) {
        super(HttpStatus.BAD_REQUEST, DEFAULT_ERROR_CODE,
                String.format("File type '%s' is not allowed", detectedMimeType));
    }

    public UnsupportedFileTypeException(String detectedMimeType, String declaredContentType) {
        super(HttpStatus.BAD_REQUEST, DEFAULT_ERROR_CODE,
                String.format("Declared content type '%s' does not match actual file content '%s'", declaredContentType, detectedMimeType));
    }

    public UnsupportedFileTypeException(String message, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, DEFAULT_ERROR_CODE, message, cause);
    }
}
