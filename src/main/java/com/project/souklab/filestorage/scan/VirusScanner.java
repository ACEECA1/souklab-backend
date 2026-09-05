package com.project.souklab.filestorage.scan;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Antivirus scanner abstraction interface.
 * Decouples file scanning from specific daemon or engine implementations.
 */
public interface VirusScanner {

    /**
     * Scans an InputStream for viruses and malicious payloads.
     *
     * @param content stream containing the file content to scan
     * @return scan result indicating clean, infected (with signature), or error
     */
    ScanResult scan(InputStream content);

    /**
     * Scans a byte array for viruses and malicious payloads.
     *
     * @param bytes byte array containing the file content to scan
     * @return scan result indicating clean, infected (with signature), or error
     */
    default ScanResult scan(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("Byte array content cannot be null");
        }
        return scan(new ByteArrayInputStream(bytes));
    }
}
