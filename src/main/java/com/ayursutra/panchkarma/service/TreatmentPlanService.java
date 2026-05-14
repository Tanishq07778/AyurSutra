package com.ayursutra.panchkarma.service;

import com.ayursutra.panchkarma.entity.*;
import com.ayursutra.panchkarma.entity.TreatmentPlan.PlanStatus;
import com.ayursutra.panchkarma.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TreatmentPlanService {

    private final TreatmentPlanRepository planRepo;
    private final PatientRepository       patientRepo;
    private final DoctorRepository        doctorRepo;
    private final ConnectionService       connectionService;

    // ── Create full treatment plan ─────────────────────────────────────────────
    public TreatmentPlan createPlan(Long doctorId, Long patientId, TreatmentPlan plan) {
        Doctor  doctor  = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + doctorId));
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));

        // Verify doctor-patient connection exists
        if (!connectionService.isConnected(patientId, doctorId)) {
            throw new IllegalStateException("Doctor and patient are not connected");
        }

        plan.setDoctor(doctor);
        plan.setPatient(patient);
        plan.setCreatedAt(LocalDateTime.now());
        plan.setUpdatedAt(LocalDateTime.now());
        if (plan.getStatus() == null) plan.setStatus(PlanStatus.ACTIVE);

        // Wire back-references on child collections
        plan.getPrescriptions().forEach(p -> p.setTreatmentPlan(plan));
        plan.getTherapies().forEach(t -> t.setTreatmentPlan(plan));
        plan.getDietItems().forEach(d -> d.setTreatmentPlan(plan));

        TreatmentPlan saved = planRepo.save(plan);
        log.info("Treatment plan created: id={} doctor={} patient={}", saved.getId(), doctorId, patientId);
        return saved;
    }

    // ── Update plan ────────────────────────────────────────────────────────────
    public TreatmentPlan updatePlan(Long planId, Long doctorId, TreatmentPlan updates) {
        TreatmentPlan plan = planRepo.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + planId));
        if (!plan.getDoctor().getId().equals(doctorId))
            throw new IllegalStateException("Not authorized to update this plan");

        // Update scalar fields
        if (updates.getTitle()           != null) plan.setTitle(updates.getTitle());
        if (updates.getStatus()          != null) plan.setStatus(updates.getStatus());
        if (updates.getDiagnosis()       != null) plan.setDiagnosis(updates.getDiagnosis());
        if (updates.getDiagnosisDetails()!= null) plan.setDiagnosisDetails(updates.getDiagnosisDetails());
        if (updates.getSymptoms()        != null) plan.setSymptoms(updates.getSymptoms());
        if (updates.getDoshaImbalance()  != null) plan.setDoshaImbalance(updates.getDoshaImbalance());
        if (updates.getSeverity()        != null) plan.setSeverity(updates.getSeverity());
        if (updates.getDoctorNotes()     != null) plan.setDoctorNotes(updates.getDoctorNotes());
        if (updates.getStartDate()       != null) plan.setStartDate(updates.getStartDate());
        if (updates.getEndDate()         != null) plan.setEndDate(updates.getEndDate());
        if (updates.getFollowUpDate()    != null) plan.setFollowUpDate(updates.getFollowUpDate());
        if (updates.getFollowUpNotes()   != null) plan.setFollowUpNotes(updates.getFollowUpNotes());

        // Replace child collections if provided
        if (updates.getPrescriptions() != null && !updates.getPrescriptions().isEmpty()) {
            plan.getPrescriptions().clear();
            updates.getPrescriptions().forEach(p -> { p.setTreatmentPlan(plan); plan.getPrescriptions().add(p); });
        }
        if (updates.getTherapies() != null && !updates.getTherapies().isEmpty()) {
            plan.getTherapies().clear();
            updates.getTherapies().forEach(t -> { t.setTreatmentPlan(plan); plan.getTherapies().add(t); });
        }
        if (updates.getDietItems() != null && !updates.getDietItems().isEmpty()) {
            plan.getDietItems().clear();
            updates.getDietItems().forEach(d -> { d.setTreatmentPlan(plan); plan.getDietItems().add(d); });
        }

        plan.setUpdatedAt(LocalDateTime.now());
        return planRepo.save(plan);
    }

    // ── Read operations ────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<TreatmentPlan> getPatientPlans(Long patientId) {
        return planRepo.findByPatient_IdOrderByCreatedAtDesc(patientId);
    }

    @Transactional(readOnly = true)
    public List<TreatmentPlan> getDoctorPlans(Long doctorId) {
        return planRepo.findByDoctor_IdOrderByCreatedAtDesc(doctorId);
    }

    @Transactional(readOnly = true)
    public List<TreatmentPlan> getPlansByDoctorAndPatient(Long doctorId, Long patientId) {
        return planRepo.findByDoctor_IdAndPatient_IdOrderByCreatedAtDesc(doctorId, patientId);
    }

    @Transactional(readOnly = true)
    public TreatmentPlan getPlanById(Long planId) {
        return planRepo.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + planId));
    }

    public void deletePlan(Long planId, Long doctorId) {
        TreatmentPlan plan = planRepo.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + planId));
        if (!plan.getDoctor().getId().equals(doctorId))
            throw new IllegalStateException("Not authorized to delete this plan");
        planRepo.deleteById(planId);
    }

    // ── Update therapy session count ───────────────────────────────────────────
    public TreatmentPlan markTherapySession(Long planId, Long therapyId) {
        TreatmentPlan plan = planRepo.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found"));
        plan.getTherapies().stream()
                .filter(t -> t.getId().equals(therapyId))
                .findFirst()
                .ifPresent(t -> {
                    int current = t.getCompletedSessions() != null ? t.getCompletedSessions() : 0;
                    int total   = t.getTotalSessions()     != null ? t.getTotalSessions()     : 1;
                    t.setCompletedSessions(Math.min(current + 1, total));
                });
        plan.setUpdatedAt(LocalDateTime.now());
        return planRepo.save(plan);
    }
}