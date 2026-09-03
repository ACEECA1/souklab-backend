package com.project.souklab.filestorage.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for the file storage module.
 * Binds to prefix "storage" in application.properties or environment.
 */
@ConfigurationProperties(prefix = "storage")
@Getter
@Setter
public class StorageProperties {

    /**
     * Storage backend provider name: "in-memory" (test/dev stub), "s3" (MinIO/AWS/R2), "local" (disk).
     */
    private String provider = "in-memory";

    /**
     * Validation rules for uploaded files.
     */
    private ValidationProperties validation = new ValidationProperties();

    @Getter
    @Setter
    public static class ValidationProperties {
        /**
         * Maximum allowed file size for uploads (e.g. 10MB).
         */
        private DataSize maxFileSize = DataSize.ofMegabytes(10);

        /**
         * List of allowed MIME types.
         */
        private List<String> allowedMimeTypes = new ArrayList<>(List.of(
                "image/jpeg",
                "image/png",
                "image/webp",
                "application/pdf"
        ));
    }
}
