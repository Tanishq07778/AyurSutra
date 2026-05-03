package com.ayursutra.panchkarma.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Data
public class DoctorRegistrationRequest {

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

    @NotBlank(message = "License number is required")
    private String licenseNumber;

    private String specialization;
    private String qualification;
    private Integer experienceYears;
    private Double consultationFee;
    private List<String> expertiseAreas;
    private LocalTime availableFrom;
    private LocalTime availableTo;

    // CHANGED: Now List<String> instead of List<Doctor.DayOfWeek>
    private List<String> workingDays;

    private String bio;
}