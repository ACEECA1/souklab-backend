package com.project.souklab.filestorage.config;

import com.project.souklab.filestorage.StorageService;
import com.project.souklab.filestorage.stub.InMemoryStorageService;
import com.project.souklab.filestorage.validation.FileValidator;
import org.apache.tika.Tika;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring auto-configuration for the file storage module.
 */
@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Tika tika() {
        return new Tika();
    }

    @Bean
    @ConditionalOnMissingBean
    public FileValidator fileValidator(StorageProperties properties, Tika tika) {
        return new FileValidator(properties, tika);
    }

    @Bean
    @ConditionalOnProperty(name = "storage.provider", havingValue = "in-memory", matchIfMissing = true)
    @ConditionalOnMissingBean(StorageService.class)
    public StorageService inMemoryStorageService() {
        return new InMemoryStorageService();
    }
}
