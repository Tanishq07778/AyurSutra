package com.ayursutra.panchkarma.dto;

import com.ayursutra.panchkarma.entity.Patient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PatientRegistrationRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
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