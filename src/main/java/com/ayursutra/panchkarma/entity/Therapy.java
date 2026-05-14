package com.ayursutra.panchkarma.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "therapies")
@Data
@NoArgsConstructor
public class Therapy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_plan_id", nullable = false)
    @JsonIgnore
    private TreatmentPlan treatmentPlan;

    @Column(nullable = false)
    private String therapyName;          // e.g. "Abhyanga", "Shirodhara", "Nasya"

    @Enumerated(EnumType.STRING)
    private TherapyType therapyType;

    @Column(length = 1000)
    private String description;

    @Column
    private Integer durationMinutes;     // Session duration in minutes

    @Column
    private Integer totalSessions;      // Total sessions prescribed

    @Column
    private Integer completedSessions;  // Tracked as patient completes

    @Column
    private String frequency;           // "Daily", "3 times a week", etc.

    @Column
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    @Column(length = 500)
    private String precautions;

    @Column(length = 500)
    private String benefits;

    public enum TherapyType {
        PANCHAKARMA,    // Core detox therapies
        ABHYANGA,       // Oil massage
        SHIRODHARA,     // Head oil stream
        NASYA,          // Nasal therapy
        BASTI,          // Enema therapy
        VIRECHANA,      // Purgation
        VAMANA,         // Emesis
        RAKTAMOKSHANA,  // Bloodletting
        SWEDANA,        // Steam therapy
        UDVARTANA,      // Herbal powder massage
        YOGA,           // Yoga therapy
        PRANAYAMA,      // Breathing exercises
        MEDITATION,     // Meditation sessions
        OTHER
    }
}