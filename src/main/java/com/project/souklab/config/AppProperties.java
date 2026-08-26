package com.project.souklab.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class AppProperties {

    private Storage storage = new Storage();
    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();
    private Admin admin = new Admin();
    private Email email = new Email();
    private Mailersend mailersend = new Mailersend();
    private Chargily chargily = new Chargily();

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
    public static class Cors {
        private List<String> allowedOrigins = new ArrayList<>();
    }

    @Data
    public static class Admin {
        private String defaultPassword;
    }

    @Data
    public static class Email {
        private boolean useSmtp = true;
    }

    @Data
    public static class Mailersend {
        private String apiKey;
        private String senderEmail = "noreply@souklab.dz";
        private String senderName = "Souklab";
    }

    @Data
    public static class Chargily {
        private String apiKey;
        private String secretKey;
        private String mode = "test";
        private String webhookSecret;
    }
}
