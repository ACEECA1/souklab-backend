package com.project.souklab.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration properties for user avatar management.
 * Binds to prefix "avatar" in application.properties or environment.
 * All default values are defined externally in application.properties.
 */
@Configuration
@ConfigurationProperties(prefix = "avatar")
@Getter
@Setter
public class AvatarProperties {

    /**
     * Maximum allowed avatars stored per user in gallery history.
     */
    private long maxPerUser;

    /**
     * Strict set of MIME types allowed for user avatar uploads.
     */
    private List<String> allowedMimeTypes;
}
