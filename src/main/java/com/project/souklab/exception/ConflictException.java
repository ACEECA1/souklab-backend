package com.project.souklab.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends AppException {

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, "CONFLICT", message);
    }

    public ConflictException(String message, Throwable cause) {
        super(HttpStatus.CONFLICT, "CONFLICT", message, cause);
    }
}
