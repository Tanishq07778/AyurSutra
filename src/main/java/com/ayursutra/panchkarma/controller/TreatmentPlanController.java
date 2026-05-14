package com.ayursutra.panchkarma.controller;

import com.ayursutra.panchkarma.dto.ApiResponse;
import com.ayursutra.panchkarma.entity.TreatmentPlan;
import com.ayursutra.panchkarma.service.TreatmentPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/treatment-plans")
@RequiredArgsConstructor
@Slf4j
public class TreatmentPlanController {

    private final TreatmentPlanService planService;

    // ── GET plans for a patient ────────────────────────────────────────────────
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<List<TreatmentPlan>>> getPatientPlans(
            @PathVariable Long patientId) {
        List<TreatmentPlan> plans = planService.getPatientPlans(patientId);
        return ResponseEntity.ok(ApiResponse.success(plans.size() + " plans", plans));
    }

    // ── GET plans by doctor ────────────────────────────────────────────────────
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<ApiResponse<List<TreatmentPlan>>> getDoctorPlans(
            @PathVariable Long doctorId) {
        List<TreatmentPlan> plans = planService.getDoctorPlans(doctorId);
        return ResponseEntity.ok(ApiResponse.success(plans.size() + " plans", plans));
    }

    // ── GET plans for a specific doctor-patient pair ───────────────────────────
    @GetMapping("/doctor/{doctorId}/patient/{patientId}")
    public ResponseEntity<ApiResponse<List<TreatmentPlan>>> getPlansByPair(
            @PathVariable Long doctorId, @PathVariable Long patientId) {
        List<TreatmentPlan> plans = planService.getPlansByDoctorAndPatient(doctorId, patientId);
        return ResponseEntity.ok(ApiResponse.success(plans.size() + " plans", plans));
    }

    // ── GET single plan ────────────────────────────────────────────────────────
    @GetMapping("/{planId}")
    public ResponseEntity<ApiResponse<TreatmentPlan>> getPlan(@PathVariable Long planId) {
        try {
            return ResponseEntity.ok(ApiResponse.success("Plan found", planService.getPlanById(planId)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ── POST create new plan ───────────────────────────────────────────────────
    @PostMapping("/doctor/{doctorId}/patient/{patientId}")
    public ResponseEntity<ApiResponse<TreatmentPlan>> createPlan(
            @PathVariable Long doctorId,
            @PathVariable Long patientId,
            @RequestBody TreatmentPlan plan) {
        try {
            TreatmentPlan saved = planService.createPlan(doctorId, patientId, plan);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Treatment plan created!", saved));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Create plan error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed: " + e.getMessage()));
        }
    }

    // ── PUT update plan ────────────────────────────────────────────────────────
    @PutMapping("/{planId}/doctor/{doctorId}")
    public ResponseEntity<ApiResponse<TreatmentPlan>> updatePlan(
            @PathVariable Long planId,
            @PathVariable Long doctorId,
            @RequestBody TreatmentPlan updates) {
        try {
            return ResponseEntity.ok(ApiResponse.success("Plan updated!", planService.updatePlan(planId, doctorId, updates)));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed: " + e.getMessage()));
        }
    }

    // ── PUT mark therapy session completed ─────────────────────────────────────
    @PutMapping("/{planId}/therapy/{therapyId}/complete-session")
    public ResponseEntity<ApiResponse<TreatmentPlan>> completeSession(
            @PathVariable Long planId, @PathVariable Long therapyId) {
        try {
            return ResponseEntity.ok(ApiResponse.success("Session marked complete!", planService.markTherapySession(planId, therapyId)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed: " + e.getMessage()));
        }
    }

    // ── DELETE plan ────────────────────────────────────────────────────────────
    @DeleteMapping("/{planId}/doctor/{doctorId}")
    public ResponseEntity<ApiResponse<Void>> deletePlan(
            @PathVariable Long planId, @PathVariable Long doctorId) {
        try {
            planService.deletePlan(planId, doctorId);
            return ResponseEntity.ok(ApiResponse.success("Plan deleted", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed: " + e.getMessage()));
        }
    }
}