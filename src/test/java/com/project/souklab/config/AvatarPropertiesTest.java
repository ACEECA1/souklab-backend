package com.project.souklab.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AvatarPropertiesTest {

    @Test
    @DisplayName("AvatarProperties uninitialized instance has no hardcoded Java defaults")
    void uninitializedInstanceHasNoJavaDefaults() {
        AvatarProperties properties = new AvatarProperties();

        assertThat(properties.getMaxPerUser()).isZero();
        assertThat(properties.getAllowedMimeTypes()).isNull();
    }

    @Test
    @DisplayName("AvatarProperties binds default configuration values")
    void bindsDefaultConfiguration() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addLast(new MapPropertySource("test", Map.of(
                "avatar.max-per-user", "10",
                "avatar.allowed-mime-types", "image/jpeg,image/png,image/webp"
        )));

        Binder binder = new Binder(ConfigurationPropertySources.from(environment.getPropertySources()));
        AvatarProperties properties = binder.bind("avatar", AvatarProperties.class).get();

        assertThat(properties.getMaxPerUser()).isEqualTo(10L);
        assertThat(properties.getAllowedMimeTypes()).containsExactly("image/jpeg", "image/png", "image/webp");
    }

    @Test
    @DisplayName("AvatarProperties binds configured values from environment/properties")
    void bindsConfiguration() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addLast(new MapPropertySource("test", Map.of(
                "avatar.max-per-user", "5",
                "avatar.allowed-mime-types", "image/png,image/webp"
        )));

        Binder binder = new Binder(ConfigurationPropertySources.from(environment.getPropertySources()));
        AvatarProperties properties = binder.bind("avatar", AvatarProperties.class).get();

        assertThat(properties.getMaxPerUser()).isEqualTo(5L);
        assertThat(properties.getAllowedMimeTypes()).containsExactly("image/png", "image/webp");
    }
}
