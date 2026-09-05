package com.project.souklab.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
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
    private OAuth oauth = new OAuth();
    private Relay relay = new Relay();
    private RateLimit rateLimit = new RateLimit();

    @Data
    public static class Relay {
        private String host;
        private int port;
        private String clientLogin;
        private String clientPasscode;
        private String systemLogin;
        private String systemPasscode;
    }

    @Data
    public static class Jwt {
        private String secret;
        private Long accessTokenExpirationMs;
        private Long refreshTokenExpirationMs;
    }

    @Data
    public static class Storage {
        private String uploadDir;
        private String thumbnails;
        private String indexes;
    }

    @Data
    public static class Cors {
        private List<String> allowedOrigins = new ArrayList<>();
    }

    @Data
    public static class Admin {
        private String defaultEmail;
        private String defaultPassword;
    }

    @Data
    public static class Email {
        private boolean useSmtp;
    }

    @Data
    public static class Mailersend {
        private String apiKey;
        private String apiUrl;
        private String senderEmail;
        private String senderName;
    }

    @Data
    public static class Chargily {
        private String apiKey;
        private String secretKey;
        private String mode;
        private String webhookSecret;
    }

    @Data
    public static class OAuth {
        private Google google = new Google();

        @Data
        public static class Google {
            private String clientId;
            private String clientSecret;
            private String redirectUri;
        }
    }

    /**
     * Global API rate-limiting configuration bound to {@code app.rate-limit.*}.
     * Defaults are supplied via environment variables in {@code application.properties};
     * no Java-side defaults are declared here.
     */
    @Data
    public static class RateLimit {
        private boolean enabled;
        private int capacity;
        private Duration refillDuration;
        private Cache cache = new Cache();

        /**
         * In-memory cache configuration for global rate limiting buckets.
         */
        @Data
        public static class Cache {
            private long maximumSize;
            private Duration expireAfterAccess;
        }
    }
}
