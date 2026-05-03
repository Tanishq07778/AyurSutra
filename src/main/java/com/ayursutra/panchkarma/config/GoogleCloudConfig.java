package com.ayursutra.panchkarma.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import java.io.File;

/**
 * GoogleCloudConfig — intentionally MINIMAL now.
 *
 * PREVIOUS BUG: This class called System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", path).
 * This set a JVM system property, but the Google SDK's ImageAnnotatorClient.create() reads
 * the OS environment variable via System.getenv("GOOGLE_APPLICATION_CREDENTIALS") — these
 * are completely separate. The property was being set but never read by the SDK, so
 * credentials were never found, and OCR silently failed every time.
 *
 * FIX: OCRService now loads credentials directly using:
 *   GoogleCredentials.fromStream(new FileInputStream(credFile))
 * and passes them to ImageAnnotatorSettings via FixedCredentialsProvider.
 * This bypasses the env var lookup entirely and works reliably in any environment.
 *
 * This config class now only logs startup confirmation — the actual credential
 * loading happens inside OCRService.extractTextFromImage().
 */
@Configuration
@Slf4j
public class GoogleCloudConfig {

    @Value("${google.application.credentials:src/main/resources/google-vision-key.json}")
    private String credentialsPath;

    @PostConstruct
    public void init() {
        File credFile = new File(credentialsPath);
        if (credFile.exists()) {
            log.info("✅ Google Vision credentials file found at: {}", credFile.getAbsolutePath());
            log.info("   OCR is ENABLED — credentials loaded on first OCR request.");
        } else {
            log.warn("⚠️  Google Vision credentials NOT found at: {}", credentialsPath);
            log.warn("   OCR is DISABLED — uploads will succeed but text extraction will be skipped.");
            log.warn("   To enable OCR: place your service account JSON at {}", credentialsPath);
        }
        // DO NOT set System.setProperty here — it has no effect on Google SDK credential lookup.
    }
}