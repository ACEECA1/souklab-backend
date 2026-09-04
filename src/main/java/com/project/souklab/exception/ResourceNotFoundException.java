package com.project.souklab.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends AppException {

    private static final String DEFAULT_ERROR_CODE = "RESOURCE_NOT_FOUND";

    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, DEFAULT_ERROR_CODE, message);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(HttpStatus.NOT_FOUND, DEFAULT_ERROR_CODE, String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(HttpStatus.NOT_FOUND, DEFAULT_ERROR_CODE, message, cause);
    }
}
