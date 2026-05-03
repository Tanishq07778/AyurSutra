package com.ayursutra.panchkarma.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "admins")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@PrimaryKeyJoinColumn(name = "user_id")
public class Admin extends User {

    @Column(name = "employee_id", unique = true, nullable = false)
    private String employeeId;

    @Column(name = "department")
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(name = "admin_level")
    private AdminLevel adminLevel = AdminLevel.BASIC;

    @ElementCollection
    @CollectionTable(name = "admin_permissions", joinColumns = @JoinColumn(name = "admin_id"))
    @Column(name = "permission")
    @Enumerated(EnumType.STRING)
    private List<Permission> permissions = new ArrayList<>();

    @Column(name = "can_approve_appointments")
    private Boolean canApproveAppointments = false;

    @Column(name = "can_manage_billing")
    private Boolean canManageBilling = false;

    @Column(name = "can_manage_users")
    private Boolean canManageUsers = false;

    @Column(name = "can_generate_reports")
    private Boolean canGenerateReports = true;

    @Column(name = "last_login")
    private java.time.LocalDateTime lastLogin;

    public enum AdminLevel {
        BASIC,
        MANAGER,
        SUPER_ADMIN
    }

    public enum Permission {
        USER_MANAGEMENT,
        DOCTOR_MANAGEMENT,
        PATIENT_MANAGEMENT,
        APPOINTMENT_MANAGEMENT,
        BILLING_MANAGEMENT,
        THERAPY_MANAGEMENT,
        REPORT_GENERATION,
        SYSTEM_CONFIGURATION,
        AUDIT_LOG_ACCESS
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }

    public boolean isSuperAdmin() {
        return adminLevel == AdminLevel.SUPER_ADMIN;
    }
}
