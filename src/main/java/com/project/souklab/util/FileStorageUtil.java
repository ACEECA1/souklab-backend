package com.project.souklab.util;

import com.project.souklab.config.AppProperties;
import com.project.souklab.exception.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
public class FileStorageUtil {
    private final Path uploadDir;

    public FileStorageUtil(AppProperties appProperties) {
        this.uploadDir = Path.of(appProperties.getStorage().getUploadDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException ex) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot initialize file storage");
        }
    }

    public String storePdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Uploaded file is required");
        }
        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
        Path target = uploadDir.resolve(fileName);
        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return target.toString();
        } catch (IOException ex) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to store uploaded file");
        }
    }

    public Path resolve(String filePath) {
        return Path.of(filePath).toAbsolutePath().normalize();
    }

    public boolean exists(String filePath) {
        return Files.exists(resolve(filePath));
    }

    public void deleteIfExists(String filePath) {
        try {
            Files.deleteIfExists(resolve(filePath));
        } catch (IOException ex) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to delete stored file");
        }
    }
}

