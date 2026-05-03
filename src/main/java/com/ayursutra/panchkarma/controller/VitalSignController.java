package com.ayursutra.panchkarma.controller;

import com.ayursutra.panchkarma.dto.ApiResponse;
import com.ayursutra.panchkarma.entity.VitalSign;
import com.ayursutra.panchkarma.service.VitalSignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/vitals")
@RequiredArgsConstructor
@Slf4j
public class VitalSignController {

    private final VitalSignService vitalSignService;

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/v1/vitals/{patientId}   — record new vitals
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/{patientId}")
    public ResponseEntity<ApiResponse<VitalSign>> recordVitals(
            @PathVariable Long patientId,
            @RequestBody VitalSign vitalSign) {

        log.info("POST /vitals/{} — recording vitals", patientId);
        try {
            VitalSign recorded = vitalSignService.recordVitals(patientId, vitalSign);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Vitals recorded successfully", recorded));
        } catch (IllegalArgumentException e) {
            // Patient not found
            log.warn("Patient not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error recording vitals for patient {}: {}", patientId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to record vitals: " + e.getMessage()));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/vitals/{patientId}    — all vitals (newest first)
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/{patientId}")
    public ResponseEntity<ApiResponse<List<VitalSign>>> getPatientVitals(
            @PathVariable Long patientId) {

        log.info("GET /vitals/{}", patientId);
        try {
            List<VitalSign> vitals = vitalSignService.getPatientVitals(patientId);
            return ResponseEntity.ok(
                    ApiResponse.success("Retrieved " + vitals.size() + " vitals", vitals));
        } catch (Exception e) {
            log.error("Error fetching vitals for patient {}: {}", patientId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch vitals: " + e.getMessage()));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/vitals/{patientId}/latest
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/{patientId}/latest")
    public ResponseEntity<ApiResponse<VitalSign>> getLatestVitals(
            @PathVariable Long patientId) {

        try {
            VitalSign vitals = vitalSignService.getLatestVitals(patientId);
            if (vitals == null) {
                return ResponseEntity.ok(
                        ApiResponse.success("No vitals recorded yet", null));
            }
            return ResponseEntity.ok(ApiResponse.success("Latest vitals", vitals));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch latest vitals: " + e.getMessage()));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/vitals/{patientId}/range?startDate=...&endDate=...
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/{patientId}/range")
    public ResponseEntity<ApiResponse<List<VitalSign>>> getVitalsByDateRange(
            @PathVariable Long patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        try {
            List<VitalSign> vitals =
                    vitalSignService.getVitalsByDateRange(patientId, startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success("Vitals in range", vitals));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch vitals: " + e.getMessage()));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/v1/vitals/{vitalId}
    // ─────────────────────────────────────────────────────────────────────────
    @DeleteMapping("/{vitalId}")
    public ResponseEntity<ApiResponse<Void>> deleteVital(@PathVariable Long vitalId) {
        try {
            vitalSignService.deleteVital(vitalId);
            return ResponseEntity.ok(ApiResponse.success("Vital deleted", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete vital: " + e.getMessage()));
        }
    }
}