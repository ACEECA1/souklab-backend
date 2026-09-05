package com.project.souklab.filestorage.scan;

import com.project.souklab.filestorage.config.StorageProperties;
import com.project.souklab.filestorage.exception.VirusDetectedException;
import com.project.souklab.filestorage.validation.ValidatedFile;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration verification test exercising ClamdInstreamScanner and VirusScanService
 * against the live ClamAV daemon running via docker-compose on localhost:3310.
 * Verifies clean file passthrough and EICAR synthetic virus detection with surfaced signature names.
 */
class ClamdIntegrationVerificationTest {

    private static final String EICAR_TEST_STRING =
            "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*";

    private VirusScanner virusScanner;
    private VirusScanService virusScanService;

    /**
     * Verifies that the ClamAV daemon is accessible on localhost:3310 before running integration tests.
     */
    @BeforeAll
    static void verifyClamDaemonAvailable() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", 3310), 1500);
        } catch (Exception e) {
            Assumptions.abort("ClamAV daemon not accessible on localhost:3310. Skipping integration test: " + e.getMessage());
        }
    }

    @BeforeEach
    void setUp() {
        StorageProperties properties = new StorageProperties();
        StorageProperties.VirusScanProperties scanProperties = new StorageProperties.VirusScanProperties();
        scanProperties.setEnabled(true);
        scanProperties.setHost("localhost");
        scanProperties.setPort(3310);
        scanProperties.setConnectionTimeout(Duration.ofSeconds(3));
        scanProperties.setReadTimeout(Duration.ofSeconds(10));
        scanProperties.setFailOpen(false);
        properties.setVirusScan(scanProperties);

        virusScanner = new ClamdInstreamScanner(scanProperties);
        virusScanService = new VirusScanService(properties, virusScanner);
    }

    /**
     * Verifies that a clean, harmless document passes through ClamAV with clean status.
     */
    @Test
    @DisplayName("integration: clean document passes ClamAV scan successfully")
    void verifyCleanFilePassesScan() throws IOException {
        byte[] cleanContent = "Souklab Algerian Artisan Marketplace Platform Document Content".getBytes(StandardCharsets.UTF_8);
        ValidatedFile cleanFile = new ValidatedFile(
                new ByteArrayInputStream(cleanContent),
                "artisan_handbook.pdf",
                "application/pdf",
                cleanContent.length
        );

        ScanResult directResult = virusScanner.scan(new ByteArrayInputStream(cleanContent));
        System.out.println("=== CLAMAV REAL SCAN (CLEAN FILE) ===");
        System.out.println("Status: " + directResult.status());
        System.out.println("Is Clean: " + directResult.isClean());
        System.out.println("Message: " + directResult.message());

        assertThat(directResult.isClean()).isTrue();
        assertThat(directResult.isInfected()).isFalse();
        assertThat(directResult.isError()).isFalse();

        ValidatedFile scanned = virusScanService.scan(cleanFile);
        assertThat(scanned).isNotNull();
        assertThat(scanned.content().readAllBytes()).isEqualTo(cleanContent);
        System.out.println("ValidatedFile successfully refreshed and stream preserved.");
    }

    /**
     * Verifies that a file containing the standard EICAR test string is detected and rejected with virus name.
     */
    @Test
    @DisplayName("integration: EICAR test string is detected and rejected with VirusDetectedException")
    void verifyEicarTestStringIsDetectedAndRejected() {
        byte[] eicarBytes = EICAR_TEST_STRING.getBytes(StandardCharsets.US_ASCII);
        ValidatedFile infectedFile = new ValidatedFile(
                new ByteArrayInputStream(eicarBytes),
                "eicar_sample.com",
                "application/octet-stream",
                eicarBytes.length
        );

        ScanResult directResult = virusScanner.scan(new ByteArrayInputStream(eicarBytes));
        System.out.println("=== CLAMAV REAL SCAN (EICAR MALWARE FILE) ===");
        System.out.println("Status: " + directResult.status());
        System.out.println("Is Infected: " + directResult.isInfected());
        System.out.println("Virus Name: " + directResult.virusName());
        System.out.println("Message: " + directResult.message());

        assertThat(directResult.isInfected()).isTrue();
        assertThat(directResult.isClean()).isFalse();
        assertThat(directResult.virusName()).containsIgnoringCase("Eicar");

        assertThatThrownBy(() -> virusScanService.scan(infectedFile))
                .isInstanceOf(VirusDetectedException.class)
                .satisfies(ex -> {
                    VirusDetectedException vde = (VirusDetectedException) ex;
                    System.out.println("Caught VirusDetectedException: " + vde.getMessage());
                    System.out.println("Surfaced Virus Name: " + vde.getVirusName());
                    assertThat(vde.getVirusName()).containsIgnoringCase("Eicar");
                });
    }
}
