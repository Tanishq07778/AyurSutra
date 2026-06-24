package com.ayursutra.panchkarma.controller;

import com.ayursutra.panchkarma.dto.ApiResponse;
import com.ayursutra.panchkarma.entity.Connection;
import com.ayursutra.panchkarma.entity.Patient;
import com.ayursutra.panchkarma.service.ConnectionService;
import com.ayursutra.panchkarma.service.PatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/connections")
@RequiredArgsConstructor
@Slf4j
public class ConnectionController {

    private final ConnectionService connectionService;
    private final PatientService    patientService;   // NEW

    /** GET /api/v1/connections/patient/{patientId} */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<List<Connection>>> getPatientConnections(@PathVariable Long patientId) {
        try {
            return ResponseEntity.ok(ApiResponse.success("Connections", connectionService.getPatientConnections(patientId)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    /** GET /api/v1/connections/doctor/{doctorId} */
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<ApiResponse<List<Connection>>> getDoctorConnections(@PathVariable Long doctorId) {
        try {
            return ResponseEntity.ok(ApiResponse.success("Connections", connectionService.getDoctorConnections(doctorId)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    /** POST /api/v1/connections/request */
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<Connection>> sendRequest(@RequestBody Map<String, Object> body) {
        try {
            Long patientId = Long.parseLong(body.get("patientId").toString());
            Long doctorId  = Long.parseLong(body.get("doctorId").toString());
            String message = body.getOrDefault("message", "").toString();
            Connection conn = connectionService.sendRequest(patientId, doctorId, message);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Request sent!", conn));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("sendRequest error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed: " + e.getMessage()));
        }
    }

    /**
     * POST /api/v1/connections/connect-by-patient-id
     *
     * NEW: Doctor-initiated instant connect by scanning QR or typing patient ID.
     * Body: { doctorId: Long, patientId: "AYR-PAT-000001" }
     *
     * Steps:
     *   1. Look up Patient by human-readable patientId string
     *   2. If no existing connection → create one and auto-accept it (CONNECTED status)
     *   3. If already CONNECTED → return existing connection (idempotent)
     *   4. If PENDING (patient sent request) → accept it
     *
     * This lets doctors instantly connect without waiting for patient approval
     * — the physical QR card / ID card IS the patient's consent.
     */
    @PostMapping("/connect-by-patient-id")
    public ResponseEntity<ApiResponse<Connection>> connectByPatientId(@RequestBody Map<String, Object> body) {
        try {
            Long doctorId       = Long.parseLong(body.get("doctorId").toString());
            String patientIdStr = body.get("patientId").toString().trim();

            // Strip QR prefix if present
            if (patientIdStr.startsWith("AYURSUTRA:")) {
                String[] parts = patientIdStr.split(":");
                if (parts.length >= 2) patientIdStr = parts[1];
            }

            Patient patient = patientService.getPatientByPatientId(patientIdStr);
            Connection conn = connectionService.connectDirectly(patient.getId(), doctorId);

            return ResponseEntity.ok(ApiResponse.success(
                    "Connected with patient " + patient.getFirstName() + " " + patient.getLastName(), conn));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("connect-by-patient-id error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<Connection>> accept(
            @PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Long doctorId = Long.parseLong(body.get("doctorId").toString());
            return ResponseEntity.ok(ApiResponse.success("Accepted!", connectionService.acceptRequest(id, doctorId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<Connection>> reject(
            @PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Long doctorId = Long.parseLong(body.get("doctorId").toString());
            return ResponseEntity.ok(ApiResponse.success("Declined.", connectionService.rejectRequest(id, doctorId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> disconnect(@PathVariable Long id) {
        connectionService.disconnect(id);
        return ResponseEntity.ok(ApiResponse.success("Disconnected", null));
    }
}