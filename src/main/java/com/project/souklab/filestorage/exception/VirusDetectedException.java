package com.project.souklab.filestorage.exception;

/**
 * Thrown when an uploaded file is identified as containing malware by the antivirus scanner.
 */
public class VirusDetectedException extends StorageException {

    private final String virusName;

    /**
     * Constructs a new VirusDetectedException with the detected malware signature name.
     *
     * @param virusName the name of the detected malware signature
     */
    public VirusDetectedException(String virusName) {
        super(String.format("Malware signature detected in uploaded file: %s", virusName));
        this.virusName = virusName;
    }

    /**
     * Returns the name of the detected virus signature.
     *
     * @return the virus signature name
     */
    public String getVirusName() {
        return virusName;
    }
}
