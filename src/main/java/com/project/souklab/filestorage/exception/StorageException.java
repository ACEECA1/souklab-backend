package com.project.souklab.filestorage.exception;

import com.project.souklab.exception.AppException;
import org.springframework.http.HttpStatus;

/**
 * Base domain exception for all file storage operations.
 * Integrates directly with Souklab's existing AppException hierarchy and ApiResponse envelope conventions.
 */
public class StorageException extends AppException {

    public StorageException(HttpStatus status, String errorCode, String message) {
        super(status, errorCode, message);
    }

    public StorageException(HttpStatus status, String errorCode, String message, Throwable cause) {
        super(status, errorCode, message, cause);
    }

    public StorageException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE_ERROR", message);
    }

    public StorageException(String message, Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE_ERROR", message, cause);
    }
}
