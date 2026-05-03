package com.ayursutra.panchkarma.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * VitalSign entity.
 *
 * IMPORTANT – field name mapping:
 * The frontend sends and reads camelCase JSON keys that match these Java field
 * names exactly (Jackson default).  Do NOT rename fields without updating the
 * frontend health-dashboard.html renderVitalsGrid() and submitVitals() too.
 *
 * Frontend sends:  systolicBP, diastolicBP, heartRate, temperature,
 *                  weight, oxygenSaturation, bloodSugar, notes, recordedBy
 * Frontend reads:  same names + recordedAt, bmi
 */
@Entity
@Table(name = "vital_signs")
@Data
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class VitalSign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Relationship ──────────────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @JsonIgnore                          // prevent circular JSON loop
    private Patient patient;

    // Expose patient ID in JSON without the full patient object
    @JsonProperty("patientId")
    public Long getPatientId() {
        return patient != null ? patient.getId() : null;
    }

    // ── Vital measurements ────────────────────────────────────────────────────

    /** Systolic blood pressure in mmHg. Frontend field: systolicBP */
    @Column(name = "systolic_bp")
    private Integer systolicBP;

    /** Diastolic blood pressure in mmHg. Frontend field: diastolicBP */
    @Column(name = "diastolic_bp")
    private Integer diastolicBP;

    /** Heart rate in bpm. Frontend field: heartRate */
    @Column(name = "heart_rate")
    private Integer heartRate;

    /** Body temperature in °F. Frontend field: temperature */
    @Column(name = "temperature")
    private Double temperature;

    /** Weight in kg. Frontend field: weight */
    @Column(name = "weight")
    private Double weight;

    /** Oxygen saturation %. Frontend field: oxygenSaturation */
    @Column(name = "oxygen_saturation")
    private Integer oxygenSaturation;

    /** Blood glucose mg/dL. Frontend field: bloodSugar */
    @Column(name = "blood_sugar")
    private Integer bloodSugar;

    /** Calculated BMI — set automatically by calculateBMI(). */
    @Column(name = "bmi")
    private Double bmi;

    // ── Meta ──────────────────────────────────────────────────────────────────

    /** Free-text notes. Frontend field: notes */
    @Column(name = "notes", length = 500)
    private String notes;

    /** Who recorded this ("Self", doctor name, etc.). Frontend field: recordedBy */
    @Column(name = "recorded_by")
    private String recordedBy;

    /**
     * Timestamp when vitals were recorded.
     *
     * FIX: @CreatedDate is set by Hibernate Envers AFTER the transaction commits,
     * so the value can be null in the response returned within the same transaction.
     * VitalSignService.recordVitals() manually sets this before saving to guarantee
     * it is always present in both the DB row AND the returned JSON.
     */
    @CreatedDate
    @Column(name = "recorded_at", nullable = false, updatable = false)
    private LocalDateTime recordedAt;

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Calculate and cache BMI from height stored on the linked Patient profile.
     * Called by VitalSignService before saving.
     */
    public void calculateBMI() {
        if (weight != null && patient != null
                && patient.getHeightCm() != null && patient.getHeightCm() > 0) {
            double heightM = patient.getHeightCm() / 100.0;
            this.bmi = Math.round((weight / (heightM * heightM)) * 10.0) / 10.0;
        }
    }

    /** Convenience: BP string for logging. */
    public String getBpString() {
        if (systolicBP != null && diastolicBP != null) {
            return systolicBP + "/" + diastolicBP;
        }
        return "—";
    }
}