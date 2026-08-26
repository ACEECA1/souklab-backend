package com.project.souklab.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class CodeGeneratorUtil {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALPHANUMERIC = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public String alphanumericCode(int size) {
        StringBuilder builder = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            builder.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return builder.toString();
    }

    public String numericCode(int size) {
        StringBuilder builder = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            builder.append(RANDOM.nextInt(10));
        }
        return builder.toString();
    }
}

