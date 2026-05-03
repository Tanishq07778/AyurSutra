package com.ayursutra.panchkarma.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;

@Service
@Slf4j
public class OCRService {

    /*
     * Reads google.application.credentials from application.properties.
     * Defaults to empty string so the app starts even with no key file.
     */
    @Value("${google.application.credentials:}")
    private String credentialsPathFromProperties;

    private boolean credentialsAvailable = false;

    /**
     * Called automatically after Spring injects @Value fields.
     *
     * ROOT CAUSE FIX:
     * application.properties sets  google.application.credentials=...
     * but the Google Vision SDK reads the ENVIRONMENT VARIABLE
     * GOOGLE_APPLICATION_CREDENTIALS, not a Spring property and not a JVM
     * system property with a lowercase key.
     *
     * Previously the code checked System.getProperty("GOOGLE_APPLICATION_CREDENTIALS")
     * which is ALWAYS null because env vars are NOT JVM system properties.
     *
     * Fix: read the Spring property value and set it as a JVM system property
     * with the exact key the SDK looks for BEFORE any Vision client is created.
     */
    @PostConstruct
    public void init() {
        // 1. Check if env var is already set externally (production deployment)
        String envVar = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");

        if (envVar != null && !envVar.isBlank()) {
            log.info("✅ OCR: Using GOOGLE_APPLICATION_CREDENTIALS from environment: {}", envVar);
            credentialsAvailable = true;
            return;
        }

        // 2. Fall back to Spring property (application.properties)
        if (credentialsPathFromProperties != null && !credentialsPathFromProperties.isBlank()) {
            File credFile = new File(credentialsPathFromProperties);
            if (credFile.exists()) {
                // *** THE CRITICAL LINE ***
                // Set as JVM system property so the Google SDK can find it.
                System.setProperty("GOOGLE_APPLICATION_CREDENTIALS",
                        credFile.getAbsolutePath());
                credentialsAvailable = true;
                log.info("✅ OCR: Google Vision credentials loaded from: {}",
                        credFile.getAbsolutePath());
            } else {
                log.warn("⚠️  OCR: Credentials file NOT found at '{}'. " +
                                "Place your google-vision-key.json there or set " +
                                "GOOGLE_APPLICATION_CREDENTIALS env var. " +
                                "Uploads will succeed but OCR text extraction will be skipped.",
                        credentialsPathFromProperties);
                credentialsAvailable = false;
            }
        } else {
            log.warn("⚠️  OCR: google.application.credentials not set in application.properties. " +
                    "OCR disabled — uploads still work.");
            credentialsAvailable = false;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Extract text from an image/PDF file using Google Cloud Vision.
     *
     * Returns EMPTY STRING (never throws) when credentials are missing so that
     * report uploads always succeed regardless of OCR availability.
     */
    public String extractTextFromImage(MultipartFile file) throws IOException {
        if (!credentialsAvailable) {
            log.info("OCR skipped (no credentials). File: {}", file.getOriginalFilename());
            return "";
        }
        try {
            return extractWithVision(file);
        } catch (Exception e) {
            log.error("OCR extraction failed for '{}': {}", file.getOriginalFilename(), e.getMessage());
            return ""; // graceful fallback — never break the upload
        }
    }

    /** Returns true only when a credentials file has been verified to exist. */
    public boolean isOcrAvailable() {
        return credentialsAvailable;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private — Vision API call
    // ─────────────────────────────────────────────────────────────────────────

    private String extractWithVision(MultipartFile file) throws Exception {
        // ImageAnnotatorClient.create() picks up GOOGLE_APPLICATION_CREDENTIALS
        // automatically from the JVM system property we set in init().
        try (com.google.cloud.vision.v1.ImageAnnotatorClient vision =
                     com.google.cloud.vision.v1.ImageAnnotatorClient.create()) {

            com.google.protobuf.ByteString imgBytes =
                    com.google.protobuf.ByteString.copyFrom(file.getBytes());

            com.google.cloud.vision.v1.Image img =
                    com.google.cloud.vision.v1.Image.newBuilder()
                            .setContent(imgBytes)
                            .build();

            com.google.cloud.vision.v1.Feature feat =
                    com.google.cloud.vision.v1.Feature.newBuilder()
                            .setType(com.google.cloud.vision.v1.Feature.Type.DOCUMENT_TEXT_DETECTION)
                            .build();

            com.google.cloud.vision.v1.AnnotateImageRequest request =
                    com.google.cloud.vision.v1.AnnotateImageRequest.newBuilder()
                            .addFeatures(feat)
                            .setImage(img)
                            .build();

            com.google.cloud.vision.v1.BatchAnnotateImagesResponse response =
                    vision.batchAnnotateImages(java.util.List.of(request));

            StringBuilder text = new StringBuilder();
            for (com.google.cloud.vision.v1.AnnotateImageResponse res :
                    response.getResponsesList()) {
                if (res.hasError()) {
                    log.error("Vision API returned error: {}", res.getError().getMessage());
                    continue;
                }
                String pageText = res.getFullTextAnnotation().getText();
                if (pageText != null) text.append(pageText);
            }

            String result = text.toString().trim();
            log.info("OCR complete. Characters extracted: {}", result.length());
            return result;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Report data parsing
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Parse structured fields out of raw OCR text and auto-detect report type.
     */
    public ReportData parseReportData(String extractedText) {
        ReportData data = new ReportData();
        data.setExtractedText(extractedText);

        if (extractedText == null || extractedText.isBlank()) {
            data.setSuggestedType("OTHER");
            data.setSuggestedTitle("Medical Document");
            return data;
        }

        String lower = extractedText.toLowerCase();

        // ── Extract patient name ──────────────────────────────────────────────
        for (String line : extractedText.split("\n")) {
            String l = line.toLowerCase();
            if (l.contains("patient name:") || l.contains("patient:") || l.contains("name:")) {
                String[] parts = line.split(":", 2);
                if (parts.length > 1 && !parts[1].isBlank()) {
                    data.setPatientName(parts[1].trim());
                    break;
                }
            }
        }

        // ── Extract report date ───────────────────────────────────────────────
        for (String line : extractedText.split("\n")) {
            if (line.toLowerCase().contains("date:")) {
                String[] parts = line.split(":", 2);
                if (parts.length > 1 && !parts[1].isBlank()) {
                    data.setReportDate(parts[1].trim());
                    break;
                }
            }
        }

        // ── Auto-detect report type from content keywords ─────────────────────
        if (lower.contains("blood test") || lower.contains("cbc")
                || lower.contains("hemoglobin") || lower.contains("platelet")
                || lower.contains("wbc") || lower.contains("rbc")
                || lower.contains("haematology")) {
            data.setSuggestedType("LAB_REPORT");
            data.setSuggestedTitle("Blood Test Report");

        } else if (lower.contains("x-ray") || lower.contains("mri")
                || lower.contains("ct scan") || lower.contains("ultrasound")
                || lower.contains("radiolog") || lower.contains("imaging")) {
            data.setSuggestedType("RADIOLOGY");
            data.setSuggestedTitle("Imaging Report");

        } else if (lower.contains("prescription") || lower.contains("rx")
                || lower.contains("dosage") || lower.contains("tablet")
                || lower.contains("capsule") || lower.contains("mg ")
                || lower.contains("syrup")) {
            data.setSuggestedType("PRESCRIPTION");
            data.setSuggestedTitle("Prescription");

        } else if (lower.contains("discharge") || lower.contains("admission")
                || lower.contains("hospital") || lower.contains("inpatient")) {
            data.setSuggestedType("DISCHARGE_SUMMARY");
            data.setSuggestedTitle("Discharge Summary");

        } else if (lower.contains("consultation") || lower.contains("examination")
                || lower.contains("diagnosis") || lower.contains("physician")) {
            data.setSuggestedType("CONSULTATION_NOTES");
            data.setSuggestedTitle("Consultation Notes");

        } else {
            data.setSuggestedType("OTHER");
            data.setSuggestedTitle("Medical Document");
        }

        return data;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Inner DTO
    // ─────────────────────────────────────────────────────────────────────────

    public static class ReportData {
        private String patientName;
        private String reportDate;
        private String suggestedType;
        private String suggestedTitle;
        private String extractedText;

        public String getPatientName()         { return patientName; }
        public void   setPatientName(String v) { patientName = v; }
        public String getReportDate()          { return reportDate; }
        public void   setReportDate(String v)  { reportDate = v; }
        public String getSuggestedType()       { return suggestedType; }
        public void   setSuggestedType(String v){ suggestedType = v; }
        public String getSuggestedTitle()      { return suggestedTitle; }
        public void   setSuggestedTitle(String v){ suggestedTitle = v; }
        public String getExtractedText()       { return extractedText; }
        public void   setExtractedText(String v){ extractedText = v; }
    }
}