package com.ayursutra.panchkarma.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "prescriptions")
@Data
@NoArgsConstructor
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_plan_id", nullable = false)
    @JsonIgnore
    private TreatmentPlan treatmentPlan;

    @Column(nullable = false)
    private String medicineName;          // e.g. "Ashwagandha Churna"

    @Column
    private String medicineType;          // TABLET / CHURNA / SYRUP / CAPSULE / OIL / OTHER

    @Column
    private String dosage;                // e.g. "500mg", "1 tsp"

    @Column
    private String frequency;            // e.g. "Twice daily", "Before meals"

    @Column
    private String duration;             // e.g. "30 days", "2 weeks"

    @Column
    private String timing;               // BEFORE_MEALS / AFTER_MEALS / EMPTY_STOMACH / BEDTIME

    @Column(length = 500)
    private String instructions;         // Special instructions

    @Column(name = "is_ayurvedic")
    private Boolean isAyurvedic = true;
}