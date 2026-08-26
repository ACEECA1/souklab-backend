package com.project.souklab.service.security;

import com.project.souklab.util.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import xyz.capybara.clamav.ClamavClient;
import xyz.capybara.clamav.commands.scan.result.ScanResult;

import java.nio.file.Path;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.project.souklab.config.AppProperties;

@Service
@RequiredArgsConstructor
@Slf4j
public class VirusScanService {

    private final AppProperties appProperties;

    /**
     * Submits a physical PDF file to a ClamAV server to scan for viruses or malicious payloads.
     * If the virus scan feature is disabled in the app properties, it bypasses the scan securely.
     *
     * @param pdfPath the absolute path on disk to the PDF file
     * @throws AppException if a virus is found, or if the connection to ClamAV fails
     */
    public void scanPdf(Path pdfPath) {
        if (!appProperties.getVirusScan().isEnabled()) {
            log.info("Virus scan is disabled. Skipping scan for: {}", pdfPath);
            return;
        }

        if (pdfPath == null) {
            throw new AppException("Virus scan failed: missing PDF path", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        try {
            ClamavClient client = new ClamavClient(appProperties.getVirusScan().getHost(), appProperties.getVirusScan().getPort());
            ScanResult result = client.scan(pdfPath);
            if (result instanceof ScanResult.VirusFound) {
                ScanResult.VirusFound virusFound = (ScanResult.VirusFound) result;
                throw new AppException("Virus detected in uploaded PDF: " + virusFound.getFoundViruses(), HttpStatus.UNPROCESSABLE_ENTITY);
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException("Unable to run virus scan: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
