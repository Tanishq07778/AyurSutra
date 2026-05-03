// ================================================================
// FILE: PatientUpdateRequest.java
// Place in: com/ayursutra/panchkarma/dto/
// ================================================================
package com.ayursutra.panchkarma.dto;

import com.ayursutra.panchkarma.entity.Patient;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PatientUpdateRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private LocalDate dateOfBirth;
    private Patient.Gender gender;
    private String address;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String bloodGroup;
    private Patient.DoshaType doshaType;
    private String medicalHistory;
    private List<String> allergies;
    private List<String> currentMedications;
    private Double heightCm;
    private Double weightKg;
}