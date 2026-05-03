package com.ayursutra.panchkarma.dto;

import com.ayursutra.panchkarma.entity.Doctor;
import com.ayursutra.panchkarma.entity.Patient;
import com.ayursutra.panchkarma.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phone;
    private User.Role role;
    private User.AccountStatus status;
    private String profilePictureUrl;
    private Boolean emailVerified;
    private LocalDateTime createdAt;

    // Optional IDs - only populated if user is Patient or Doctor
    private String patientId;
    private String doctorId;

    public static UserResponse from(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setFullName(user.getFullName());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setProfilePictureUrl(user.getProfilePictureUrl());
        response.setEmailVerified(user.getEmailVerified());
        response.setCreatedAt(user.getCreatedAt());

        // Set patient ID if user is a Patient
        if (user instanceof Patient) {
            Patient patient = (Patient) user;
            response.setPatientId(patient.getPatientId());
        }

        // Set doctor ID if user is a Doctor
        if (user instanceof Doctor) {
            Doctor doctor = (Doctor) user;
            response.setDoctorId(doctor.getDoctorId());
        }

        return response;
    }
}