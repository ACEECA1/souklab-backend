package com.project.souklab.filestorage.stub;

import com.project.souklab.filestorage.StorageResource;
import com.project.souklab.filestorage.StorageResult;
import com.project.souklab.filestorage.StorageService;
import com.project.souklab.filestorage.exception.FileNotFoundStorageException;
import com.project.souklab.filestorage.exception.FileTooLargeException;
import com.project.souklab.filestorage.exception.StorageException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Minimal in-memory implementation of {@link StorageService} backed by a {@link ConcurrentHashMap}.
 *
 * <p><strong>DEVELOPMENT / TEST STUB ONLY:</strong>
 * This class exists solely to prove the {@link StorageService} interface, metadata model,
 * and pre-storage validation pipeline in Phase D.0a without requiring external infrastructure.
 * It is NOT suitable for production and will be accompanied by S3/MinIO in Phase D.0b.
 */
@Slf4j
public class InMemoryStorageService implements StorageService {

    private final Map<String, StoredFile> store = new ConcurrentHashMap<>();

    private record StoredFile(
            byte[] data,
            String originalFilename,
            String contentType,
            Instant storedAt
    ) {}

    @Override
    public StorageResult store(InputStream content, String originalFilename, String contentType, long size) {
        if (content == null) {
            throw new StorageException("Cannot store null content stream");
        }

        byte[] bytes;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            content.transferTo(buffer);
            bytes = buffer.toByteArray();
        } catch (FileTooLargeException e) {
            throw e;
        } catch (IOException e) {
            if (e.getCause() instanceof FileTooLargeException ftle) {
                throw ftle;
            }
            throw new StorageException("Failed to read input content stream for storage", e);
        }

        // Generate safe, opaque UUID-based storage key (NEVER using original filename as path)
        String extension = extractExtension(originalFilename);
        String key = UUID.randomUUID().toString() + (extension.isEmpty() ? "" : "." + extension);

        Instant now = Instant.now();
        store.put(key, new StoredFile(bytes, originalFilename, contentType, now));
        log.debug("Stored file in-memory with key '{}' (size: {} bytes, original: '{}')", key, bytes.length, originalFilename);

        return new StorageResult(key, originalFilename, contentType, bytes.length, now);
    }

    @Override
    public StorageResource retrieve(String key) {
        StoredFile file = store.get(key);
        if (file == null) {
            throw new FileNotFoundStorageException(key);
        }
        return new StorageResource(
                key,
                new ByteArrayInputStream(file.data()),
                file.contentType(),
                file.data().length,
                file.originalFilename()
        );
    }

    @Override
    public void delete(String key) {
        store.remove(key);
        log.debug("Deleted in-memory file with key '{}'", key);
    }

    @Override
    public boolean exists(String key) {
        return store.containsKey(key);
    }

    /**
     * Helper to clean up all in-memory entries (useful between tests).
     */
    public void clear() {
        store.clear();
    }

    private String extractExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1).toLowerCase();
        }
        return "";
    }
}
