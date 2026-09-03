package com.project.souklab;

import com.project.souklab.filestorage.config.StorageProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Basic Spring Boot context loading test.
 * Also verifies property binding from application.properties.
 */
@SpringBootTest
class SouklabApplicationTests {

    @Autowired
    private StorageProperties storageProperties;

    /**
     * Verifies that the Spring application context loads successfully
     * and that storage properties bind correctly from application.properties.
     */
    @Test
    void contextLoads() {
        List<String> mimeTypes = storageProperties.getValidation().getAllowedMimeTypes();
        System.out.println("=== STORAGE PROPERTIES BINDING CHECK ===");
        System.out.println("Raw allowedMimeTypes list: " + mimeTypes);
        System.out.println("List element count: " + (mimeTypes != null ? mimeTypes.size() : 0));
        if (mimeTypes != null) {
            for (int i = 0; i < mimeTypes.size(); i++) {
                System.out.println("  [" + i + "] " + mimeTypes.get(i));
            }
        }
        assertThat(mimeTypes)
                .isNotNull()
                .hasSize(4)
                .containsExactly("image/jpeg", "image/png", "image/webp", "application/pdf");
    }

}
