package com.ayursutra.panchkarma.controller;

import com.ayursutra.panchkarma.dto.ApiResponse;
import com.ayursutra.panchkarma.entity.MedicalReport;
import com.ayursutra.panchkarma.service.FileStorageService;
import com.ayursutra.panchkarma.service.MedicalReportService;
import com.ayursutra.panchkarma.service.OCRService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Slf4j
public class MedicalReportController {

    private final MedicalReportService reportService;
    private final FileStorageService   fileStorageService;
    private final OCRService           ocrService;

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/reports/{patientId}
    // Returns all reports for a patient, newest first.
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/{patientId}")
    public ResponseEntity<ApiResponse<List<MedicalReport>>> getPatientReports(
            @PathVariable Long patientId) {

        log.info("GET /reports/{}", patientId);
        try {
            List<MedicalReport> reports = reportService.getPatientReports(patientId);
            return ResponseEntity.ok(
                    ApiResponse.success("Retrieved " + reports.size() + " reports", reports));
        } catch (Exception e) {
            log.error("Error fetching reports for patient {}: {}", patientId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch reports: " + e.getMessage()));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/reports/detail/{reportId}
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/detail/{reportId}")
    public ResponseEntity<ApiResponse<MedicalReport>> getReportById(
            @PathVariable Long reportId) {

        try {
            MedicalReport report = reportService.getReportById(reportId);
            return ResponseEntity.ok(ApiResponse.success("Report found", report));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch report: " + e.getMessage()));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/v1/reports/{patientId}/upload   — simple upload (no OCR)
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/{patientId}/upload")
    public ResponseEntity<ApiResponse<MedicalReport>> uploadReport(
            @PathVariable Long patientId,
            @RequestParam("file")                              MultipartFile file,
            @RequestParam("title")                             String title,
            @RequestParam("reportType")                        String reportType,
            @RequestParam(value = "description",  required = false) String description,
            @RequestParam(value = "reportDate",   required = false) String reportDateStr) {

        log.info("POST /reports/{}/upload — file={} type={}", patientId,
                file.getOriginalFilename(), reportType);
        try {
            String filePath = fileStorageService.saveFile(file);

            MedicalReport report = buildReport(
                    title, reportType, description, reportDateStr,
                    filePath, file, null, null);

            MedicalReport saved = reportService.uploadReport(patientId, report);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Report uploaded successfully", saved));

        } catch (Exception e) {
            log.error("Upload failed for patient {}: {}", patientId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to upload report: " + e.getMessage()));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/v1/reports/{patientId}/upload-with-ocr   — upload + OCR
    //
    // FIX: Previously OCR exceptions propagated and caused a 500.
    //      Now OCR is attempted and on any failure the upload still succeeds
    //      with a note explaining why OCR was skipped.
    //      The extracted text is stored in MedicalReport.extractedText so it
    //      can be viewed later — it was previously only put in doctorNotes.
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/{patientId}/upload-with-ocr")
    public ResponseEntity<ApiResponse<MedicalReport>> uploadReportWithOCR(
            @PathVariable Long patientId,
            @RequestParam("file")                              MultipartFile file,
            @RequestParam("title")                             String title,
            @RequestParam("reportType")                        String reportType,
            @RequestParam(value = "description",  required = false) String description,
            @RequestParam(value = "reportDate",   required = false) String reportDateStr) {

        log.info("POST /reports/{}/upload-with-ocr — file={} ocrAvailable={}",
                patientId, file.getOriginalFilename(), ocrService.isOcrAvailable());

        String extractedText = "";
        String ocrNote       = "";
        String successMsg;

        // ── OCR attempt (never blocks the upload) ─────────────────────────────
        try {
            extractedText = ocrService.extractTextFromImage(file);

            if (extractedText == null || extractedText.isBlank()) {
                ocrNote    = ocrService.isOcrAvailable()
                        ? "OCR ran but found no text in this file."
                        : "OCR unavailable — google-vision-key.json not configured. " +
                        "See application.properties: google.application.credentials";
                successMsg = "Report uploaded (OCR found no text)";
            } else {
                OCRService.ReportData parsed = ocrService.parseReportData(extractedText);
                ocrNote    = "Auto-detected type: " + parsed.getSuggestedType();
                successMsg = "Report uploaded — " + extractedText.length() + " characters extracted";
                log.info("OCR success: {} chars, suggested type: {}",
                        extractedText.length(), parsed.getSuggestedType());
            }
        } catch (Exception ocrEx) {
            log.warn("OCR error (upload will still succeed): {}", ocrEx.getMessage());
            ocrNote    = "OCR error: " + ocrEx.getMessage();
            successMsg = "Report uploaded (OCR encountered an error)";
        }

        // ── Save file + report ────────────────────────────────────────────────
        try {
            String filePath = fileStorageService.saveFile(file);

            MedicalReport report = buildReport(
                    title, reportType, description, reportDateStr,
                    filePath, file, extractedText, ocrNote);

            MedicalReport saved = reportService.uploadReport(patientId, report);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(successMsg, saved));

        } catch (Exception e) {
            log.error("Upload-with-ocr failed for patient {}: {}", patientId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to process report: " + e.getMessage()));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/v1/reports/{reportId}
    // ─────────────────────────────────────────────────────────────────────────
    @DeleteMapping("/{reportId}")
    public ResponseEntity<ApiResponse<Void>> deleteReport(@PathVariable Long reportId) {
        try {
            reportService.deleteReport(reportId);
            return ResponseEntity.ok(ApiResponse.success("Report deleted", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete: " + e.getMessage()));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    private MedicalReport buildReport(
            String title, String reportType, String description,
            String reportDateStr, String filePath,
            MultipartFile file, String extractedText, String ocrNote) {

        MedicalReport report = new MedicalReport();
        report.setTitle(title);
        report.setReportType(MedicalReport.ReportType.valueOf(reportType));
        report.setDescription(description);
        report.setFilePath(filePath);
        report.setFileName(file.getOriginalFilename());
        report.setFileType(file.getContentType());
        report.setFileSize(file.getSize());
        report.setUploadedBy("Patient");

        // Store OCR output in the dedicated column, not just in doctorNotes
        if (extractedText != null && !extractedText.isBlank()) {
            report.setExtractedText(extractedText);
        }
        if (ocrNote != null && !ocrNote.isBlank()) {
            report.setDoctorNotes(ocrNote);
        }

        // Parse report date from form (ISO string) or default to now
        if (reportDateStr != null && !reportDateStr.isBlank()) {
            try {
                // Remove trailing Z for LocalDateTime.parse()
                report.setReportDate(
                        LocalDateTime.parse(reportDateStr.replace("Z", "")));
            } catch (Exception ignored) {
                report.setReportDate(LocalDateTime.now());
            }
        } else {
            report.setReportDate(LocalDateTime.now());
        }

        return report;
    }
}