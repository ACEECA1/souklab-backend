package com.project.souklab.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class AppProperties {

    private Storage storage = new Storage();
    private VirusScan virusScan = new VirusScan();
    private Jwt jwt = new Jwt();

    @Data
    public static class Jwt {
        private String secret;
        private long accessTokenExpirationMs = 3600000;
        private long refreshTokenExpirationMs = 86400000;
    }

    @Data
    public static class Storage {
        private String uploadDir = "storage/uploads";
        private String thumbnails = "storage/thumbnails";
        private String indexes = "storage/indexes";
    }

    @Data
    public static class VirusScan {
        private boolean enabled = true;
        private String host = "localhost";
        private int port = 3310;
    }
}
