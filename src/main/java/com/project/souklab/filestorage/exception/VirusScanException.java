package com.project.souklab.filestorage.exception;

/**
 * Thrown when an error occurs during antivirus scanning and the configured policy is fail-closed.
 */
public class VirusScanException extends StorageException {

    /**
     * Constructs a new VirusScanException with the specified error message.
     *
     * @param message detail error message
     */
    public VirusScanException(String message) {
        super(message);
    }

    /**
     * Constructs a new VirusScanException with the specified error message and cause.
     *
     * @param message detail error message
     * @param cause underlying cause
     */
    public VirusScanException(String message, Throwable cause) {
        super(message, cause);
    }
}
