package com.ayursutra.panchkarma.controller;

import com.ayursutra.panchkarma.dto.ApiResponse;
import com.ayursutra.panchkarma.dto.PatientRegistrationRequest;
import com.ayursutra.panchkarma.dto.PatientUpdateRequest;
import com.ayursutra.panchkarma.dto.UserResponse;
import com.ayursutra.panchkarma.entity.Patient;
import com.ayursutra.panchkarma.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    /** POST /api/v1/patients/register */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> registerPatient(
            @Valid @RequestBody PatientRegistrationRequest request) {
        UserResponse patient = patientService.registerPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Patient registered successfully", patient));
    }

    /** GET /api/v1/patients */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllPatients() {
        List<UserResponse> patients = patientService.getAllPatients();
        return ResponseEntity.ok(ApiResponse.success("Retrieved " + patients.size() + " patients", patients));
    }

    /** GET /api/v1/patients/qr/{qrCode} */
    @GetMapping("/qr/{qrCode}")
    public ResponseEntity<ApiResponse<Patient>> getPatientByQRCode(@PathVariable String qrCode) {
        Patient patient = patientService.getPatientByQRCode(qrCode);
        return ResponseEntity.ok(ApiResponse.success("Patient found", patient));
    }

    /** GET /api/v1/patients/profile/{id} */
    @GetMapping("/profile/{id}")
    public ResponseEntity<ApiResponse<Patient>> getPatientProfile(@PathVariable Long id) {
        Patient patient = patientService.getPatientEntityById(id);
        return ResponseEntity.ok(ApiResponse.success("Patient profile retrieved", patient));
    }

    /** GET /api/v1/patients/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getPatientById(@PathVariable Long id) {
        UserResponse patient = patientService.getPatientById(id);
        return ResponseEntity.ok(ApiResponse.success("Patient found", patient));
    }

    /**
     * PUT /api/v1/patients/{id}/update
     * Edit Profile endpoint — updates only the fields that are provided (non-null).
     * Uses a separate PatientUpdateRequest DTO (no password/email change here).
     */
    @PutMapping("/{id}/update")
    public ResponseEntity<ApiResponse<UserResponse>> updatePatientProfile(
            @PathVariable Long id,
            @RequestBody PatientUpdateRequest request) {
        try {
            UserResponse updatedPatient = patientService.updatePatientProfile(id, request);
            return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updatedPatient));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update profile: " + e.getMessage()));
        }
    }

    /** PUT /api/v1/patients/{id} (original full update) */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientRegistrationRequest request) {
        UserResponse updatedPatient = patientService.updatePatient(id, request);
        return ResponseEntity.ok(ApiResponse.success("Patient updated successfully", updatedPatient));
    }

    /** DELETE /api/v1/patients/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.ok(ApiResponse.success("Patient deleted successfully", null));
    }
}