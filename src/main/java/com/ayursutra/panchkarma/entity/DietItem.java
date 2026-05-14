package com.ayursutra.panchkarma.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "diet_items")
@Data
@NoArgsConstructor
public class DietItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_plan_id", nullable = false)
    @JsonIgnore
    private TreatmentPlan treatmentPlan;

    @Column(nullable = false)
    private String itemName;             // Food / drink / herb name

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DietCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecommendationType recommendationType;  // EAT / AVOID / LIMIT

    @Column
    private String mealTime;            // MORNING / AFTERNOON / EVENING / ANY

    @Column(length = 500)
    private String reason;              // Why this food helps or should be avoided

    @Column(length = 300)
    private String quantity;            // Suggested amount e.g. "1 cup daily"

    public enum DietCategory {
        FRUITS, VEGETABLES, GRAINS, DAIRY, PROTEIN,
        SPICES, HERBS, BEVERAGES, SWEETS, OILS, OTHER
    }

    public enum RecommendationType {
        EAT,      // Recommended to consume
        AVOID,    // Should not consume
        LIMIT     // Consume in moderation
    }
}