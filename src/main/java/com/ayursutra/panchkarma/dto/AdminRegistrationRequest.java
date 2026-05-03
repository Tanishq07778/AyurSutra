package com.ayursutra.panchkarma.dto;

import com.ayursutra.panchkarma.entity.Admin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
public class AdminRegistrationRequest {

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

    @NotBlank(message = "Employee ID is required")
    private String employeeId;

    private String department;
    private Admin.AdminLevel adminLevel;
    private List<Admin.Permission> permissions;
    private Boolean canApproveAppointments;
    private Boolean canManageBilling;
    private Boolean canManageUsers;
    private Boolean canGenerateReports;
}