package com.ayursutra.panchkarma.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "patients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@PrimaryKeyJoinColumn(name = "user_id")
public class Patient extends User {

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(length = 500)
    private String address;

    @Column(name = "emergency_contact_name")
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone")
    private String emergencyContactPhone;

    @Column(name = "blood_group")
    private String bloodGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "dosha_type")
    private DoshaType doshaType;

    @Column(name = "medical_history", length = 2000)
    private String medicalHistory;

    @ElementCollection
    @CollectionTable(name = "patient_allergies", joinColumns = @JoinColumn(name = "patient_id"))
    @Column(name = "allergy")
    private List<String> allergies = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "patient_medications", joinColumns = @JoinColumn(name = "patient_id"))
    @Column(name = "medication")
    private List<String> currentMedications = new ArrayList<>();

    @Column(name = "height_cm")
    private Double heightCm;

    @Column(name = "weight_kg")
    private Double weightKg;

    @Column(name = "qr_code", unique = true)
    private String qrCode;

    @Column(name = "patient_id", unique = true)  // ADD THIS
    private String patientId;                     // ADD THIS


    public enum Gender {
        MALE,
        FEMALE,
        OTHER
    }

    public enum DoshaType {
        VATA,
        PITTA,
        KAPHA,
        VATA_PITTA,
        PITTA_KAPHA,
        VATA_KAPHA,
        TRIDOSHA
    }

    public Integer getAge() {
        if (dateOfBirth == null) return null;
        return LocalDate.now().getYear() - dateOfBirth.getYear();
    }

    public Double getBMI() {
        if (heightCm == null || weightKg == null || heightCm == 0) return null;
        double heightM = heightCm / 100.0;
        return weightKg / (heightM * heightM);
    }

}