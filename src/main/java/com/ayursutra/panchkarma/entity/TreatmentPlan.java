package com.ayursutra.panchkarma.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * TreatmentPlan — the core clinical record a doctor creates for a connected patient.
 *
 * One plan can contain:
 *  - Disease / diagnosis details
 *  - Prescriptions (medicines with dosage)
 *  - Panchakarma therapy sessions
 *  - Diet recommendations
 *  - Follow-up notes
 */
@Entity
@Table(name = "treatment_plans")
@Data
@NoArgsConstructor
public class TreatmentPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Relationships ──────────────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", nullable = false)
    @JsonIgnoreProperties({"password","currentMedications","allergies","hibernateLazyInitializer"})
    private Patient patient;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doctor_id", nullable = false)
    @JsonIgnoreProperties({"password","hibernateLazyInitializer"})
    private Doctor doctor;

    // ── Plan meta ──────────────────────────────────────────────────────────────
    @Column(nullable = false)
    private String title;                    // e.g. "Vata Imbalance Treatment Plan"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanStatus status = PlanStatus.ACTIVE;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Disease / Diagnosis ────────────────────────────────────────────────────
    @Column(name = "diagnosis", length = 1000)
    private String diagnosis;               // Primary diagnosis

    @Column(name = "diagnosis_details", columnDefinition = "TEXT")
    private String diagnosisDetails;        // Detailed description

    @Column(name = "symptoms", length = 2000)
    private String symptoms;                // Comma-separated or free text

    @Column(name = "dosha_imbalance")
    private String doshaImbalance;          // e.g. "Vata-Pitta aggravation"

    @Column(name = "severity")
    private String severity;                // MILD / MODERATE / SEVERE

    // ── Prescriptions (stored as JSON-like text, parsed by frontend) ───────────
    @OneToMany(mappedBy = "treatmentPlan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Prescription> prescriptions = new ArrayList<>();

    // ── Therapies ──────────────────────────────────────────────────────────────
    @OneToMany(mappedBy = "treatmentPlan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Therapy> therapies = new ArrayList<>();

    // ── Diet plan ─────────────────────────────────────────────────────────────
    @OneToMany(mappedBy = "treatmentPlan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DietItem> dietItems = new ArrayList<>();

    // ── Doctor notes ──────────────────────────────────────────────────────────
    @Column(name = "doctor_notes", columnDefinition = "TEXT")
    private String doctorNotes;

    @Column(name = "follow_up_date")
    private LocalDate followUpDate;

    @Column(name = "follow_up_notes", length = 1000)
    private String followUpNotes;

    public enum PlanStatus { ACTIVE, COMPLETED, ON_HOLD, CANCELLED }
}