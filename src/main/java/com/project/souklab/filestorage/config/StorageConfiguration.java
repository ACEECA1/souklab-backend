package com.project.souklab.filestorage.config;

import com.project.souklab.filestorage.StorageService;
import com.project.souklab.filestorage.s3.S3StorageService;
import com.project.souklab.filestorage.stub.InMemoryStorageService;
import com.project.souklab.filestorage.validation.FileValidator;
import org.apache.tika.Tika;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

/**
 * Spring auto-configuration for the file storage module.
 * Provides beans for Tika MIME sniffing, file validation, and conditionally
 * registers either in-memory or S3-compatible backend storage services.
 */
@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfiguration {

    /**
     * Provides an Apache Tika instance for MIME type sniffing via content magic bytes.
     *
     * @return a configured Tika instance
     */
    @Bean
    @ConditionalOnMissingBean
    public Tika tika() {
        return new Tika();
    }

    /**
     * Provides the provider-agnostic file validator and sanitizer component.
     *
     * @param properties configuration properties for file validation
     * @param tika Apache Tika detector instance
     * @return a configured FileValidator
     */
    @Bean
    @ConditionalOnMissingBean
    public FileValidator fileValidator(StorageProperties properties, Tika tika) {
        return new FileValidator(properties, tika);
    }

    /**
     * Registers the in-memory storage service bean when {@code storage.provider=in-memory} (or omitted).
     *
     * @return an InMemoryStorageService bean
     */
    @Bean
    @ConditionalOnProperty(name = "storage.provider", havingValue = "in-memory", matchIfMissing = true)
    @ConditionalOnMissingBean(StorageService.class)
    public StorageService inMemoryStorageService() {
        return new InMemoryStorageService();
    }

    /**
     * Registers the AWS SDK v2 {@link S3Client} when {@code storage.provider=s3}.
     * Validates that access key and secret key are present, failing fast at startup
     * with an {@link IllegalStateException} if either is missing or blank.
     *
     * @param properties file storage properties containing S3 settings
     * @return a configured S3Client
     * @throws IllegalStateException if credentials are missing or blank
     */
    @Bean
    @ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
    @ConditionalOnMissingBean(S3Client.class)
    public S3Client s3Client(StorageProperties properties) {
        StorageProperties.S3Properties s3 = properties.getS3();
        if (s3 == null
                || s3.getAccessKey() == null || s3.getAccessKey().isBlank()
                || s3.getSecretKey() == null || s3.getSecretKey().isBlank()) {
            throw new IllegalStateException("storage.s3.access-key and storage.s3.secret-key are required when storage.provider=s3");
        }

        String region = (s3.getRegion() != null && !s3.getRegion().isBlank()) ? s3.getRegion() : "us-east-1";
        boolean pathStyle = Boolean.TRUE.equals(s3.getPathStyleAccess());

        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(s3.getAccessKey(), s3.getSecretKey())
                ))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(pathStyle)
                        .build());

        if (s3.getEndpoint() != null && !s3.getEndpoint().isBlank()) {
            builder.endpointOverride(URI.create(s3.getEndpoint()));
        }

        return builder.build();
    }

    /**
     * Registers the S3 storage service backend bean when {@code storage.provider=s3}.
     *
     * @param properties file storage properties
     * @param s3Client the configured S3Client
     * @return an S3StorageService bean
     */
    @Bean
    @ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
    @ConditionalOnMissingBean(StorageService.class)
    public StorageService s3StorageService(StorageProperties properties, S3Client s3Client) {
        return new S3StorageService(properties, s3Client);
    }
}
