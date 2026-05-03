package com.ayursutra.panchkarma.controller;

import com.ayursutra.panchkarma.dto.ApiResponse;
import com.ayursutra.panchkarma.dto.DoctorRegistrationRequest;
import com.ayursutra.panchkarma.dto.UserResponse;
import com.ayursutra.panchkarma.entity.Doctor;
import com.ayursutra.panchkarma.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    /**
     * Register a new doctor (MUST BE FIRST!)
     * POST /api/v1/doctors/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> registerDoctor(
            @Valid @RequestBody DoctorRegistrationRequest request) {

        UserResponse doctor = doctorService.registerDoctor(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Doctor registered successfully", doctor));
    }

    /**
     * Get all doctors
     * GET /api/v1/doctors
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllDoctors() {
        List<UserResponse> doctors = doctorService.getAllDoctors();
        return ResponseEntity.ok(ApiResponse.success(
                "Retrieved " + doctors.size() + " doctors", doctors));
    }

    /**
     * Get available doctors only
     * GET /api/v1/doctors/available
     */
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAvailableDoctors() {
        List<UserResponse> doctors = doctorService.getAvailableDoctors();
        return ResponseEntity.ok(ApiResponse.success(
                "Retrieved " + doctors.size() + " available doctors", doctors));
    }

    /**
     * Get doctors by specialization
     * GET /api/v1/doctors/specialization/{specialization}
     */
    @GetMapping("/specialization/{specialization}")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getDoctorsBySpecialization(
            @PathVariable String specialization) {

        List<UserResponse> doctors = doctorService.getDoctorsBySpecialization(specialization);
        return ResponseEntity.ok(ApiResponse.success(
                "Retrieved " + doctors.size() + " doctors", doctors));
    }

    /**
     * Get doctor's complete profile
     * GET /api/v1/doctors/profile/{id}
     */
    @GetMapping("/profile/{id}")
    public ResponseEntity<ApiResponse<Doctor>> getDoctorProfile(@PathVariable Long id) {
        Doctor doctor = doctorService.getDoctorEntityById(id);
        return ResponseEntity.ok(ApiResponse.success("Doctor profile retrieved", doctor));
    }

    /**
     * Get doctor by ID (MUST BE AFTER specific paths!)
     * GET /api/v1/doctors/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getDoctorById(@PathVariable Long id) {
        UserResponse doctor = doctorService.getDoctorById(id);
        return ResponseEntity.ok(ApiResponse.success("Doctor found", doctor));
    }

    /**
     * Update doctor information
     * PUT /api/v1/doctors/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateDoctor(
            @PathVariable Long id,
            @Valid @RequestBody DoctorRegistrationRequest request) {

        UserResponse updatedDoctor = doctorService.updateDoctor(id, request);
        return ResponseEntity.ok(ApiResponse.success("Doctor updated successfully", updatedDoctor));
    }

    /**
     * Delete doctor
     * DELETE /api/v1/doctors/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.ok(ApiResponse.success("Doctor deleted successfully", null));
    }

    /**
     * Update doctor availability status
     * PATCH /api/v1/doctors/{id}/availability
     */
    @PatchMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<Void>> updateAvailability(
            @PathVariable Long id,
            @RequestParam boolean isAvailable) {

        doctorService.updateAvailability(id, isAvailable);
        return ResponseEntity.ok(ApiResponse.success(
                "Doctor availability updated to: " + isAvailable, null));
    }
}