package com.ayursutra.panchkarma.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "doctors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@PrimaryKeyJoinColumn(name = "user_id")
public class Doctor extends User {

    @Column(name = "license_number", unique = true, nullable = false)
    private String licenseNumber;

    @Column(name = "doctor_id", unique = true)  // ADD THIS
    private String doctorId;                     // ADD THIS

    @Column(name = "specialization")
    private String specialization;

    @Column(name = "qualification", length = 500)
    private String qualification;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "consultation_fee")
    private Double consultationFee;

    @ElementCollection
    @CollectionTable(name = "doctor_expertise", joinColumns = @JoinColumn(name = "doctor_id"))
    @Column(name = "expertise")
    private List<String> expertiseAreas = new ArrayList<>();

    @Column(name = "available_from")
    private LocalTime availableFrom;

    @Column(name = "available_to")
    private LocalTime availableTo;

    // CHANGED: Now stores as List<String> instead of List<DayOfWeek>
    @ElementCollection
    @CollectionTable(name = "doctor_working_days", joinColumns = @JoinColumn(name = "doctor_id"))
    @Column(name = "day_of_week")
    private List<String> workingDays = new ArrayList<>();

    @Column(name = "is_available")
    private Boolean isAvailable = true;

    @Column(name = "rating")
    private Double rating = 0.0;

    @Column(name = "total_consultations")
    private Integer totalConsultations = 0;

    @Column(name = "bio", length = 1000)
    private String bio;

    // Keep enum for validation/reference (optional)
    public enum DayOfWeek {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    }

    public boolean isAvailableToday() {
        String today = java.time.LocalDate.now().getDayOfWeek().toString();
        return isAvailable && workingDays.contains(today);
    }


}