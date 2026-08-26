package com.project.souklab.util;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AppException extends RuntimeException {
    private final HttpStatus status;

    public AppException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public AppException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
