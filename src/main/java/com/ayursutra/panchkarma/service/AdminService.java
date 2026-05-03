package com.ayursutra.panchkarma.service;

import com.ayursutra.panchkarma.dto.AdminRegistrationRequest;
import com.ayursutra.panchkarma.dto.UserResponse;
import com.ayursutra.panchkarma.entity.Admin;
import com.ayursutra.panchkarma.entity.User;
import com.ayursutra.panchkarma.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse registerAdmin(AdminRegistrationRequest request) {
        // Check if email already exists
        if (adminRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Check if employee ID already exists
        if (adminRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new IllegalArgumentException("Employee ID already registered");
        }

        // Create new admin
        Admin admin = new Admin();
        admin.setFirstName(request.getFirstName());
        admin.setLastName(request.getLastName());
        admin.setEmail(request.getEmail());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setPhone(request.getPhone());
        admin.setRole(User.Role.ADMIN);
        admin.setStatus(User.AccountStatus.ACTIVE);

        // Set admin-specific fields
        admin.setEmployeeId(request.getEmployeeId());
        admin.setDepartment(request.getDepartment());
        admin.setAdminLevel(request.getAdminLevel() != null ?
                request.getAdminLevel() : Admin.AdminLevel.BASIC);
        admin.setPermissions(request.getPermissions());
        admin.setCanApproveAppointments(request.getCanApproveAppointments() != null ?
                request.getCanApproveAppointments() : false);
        admin.setCanManageBilling(request.getCanManageBilling() != null ?
                request.getCanManageBilling() : false);
        admin.setCanManageUsers(request.getCanManageUsers() != null ?
                request.getCanManageUsers() : false);
        admin.setCanGenerateReports(request.getCanGenerateReports() != null ?
                request.getCanGenerateReports() : true);

        Admin savedAdmin = adminRepository.save(admin);
        return com.ayursutra.panchkarma.dto.UserResponse.from(savedAdmin);
    }

    public UserResponse getAdminById(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found with id: " + id));
        return com.ayursutra.panchkarma.dto.UserResponse.from(admin);
    }

    public Admin getAdminEntityById(Long id) {
        return adminRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found with id: " + id));
    }

    public List<UserResponse> getAllAdmins() {
        return adminRepository.findAll()
                .stream()
                .map(com.ayursutra.panchkarma.dto.UserResponse::from)
                .collect(Collectors.toList());
    }

    public UserResponse updateAdmin(Long id, AdminRegistrationRequest request) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found with id: " + id));

        // Update basic info
        admin.setFirstName(request.getFirstName());
        admin.setLastName(request.getLastName());
        admin.setPhone(request.getPhone());

        // Update admin-specific fields
        admin.setDepartment(request.getDepartment());
        admin.setAdminLevel(request.getAdminLevel());
        admin.setPermissions(request.getPermissions());
        admin.setCanApproveAppointments(request.getCanApproveAppointments());
        admin.setCanManageBilling(request.getCanManageBilling());
        admin.setCanManageUsers(request.getCanManageUsers());
        admin.setCanGenerateReports(request.getCanGenerateReports());

        Admin updatedAdmin = adminRepository.save(admin);
        return com.ayursutra.panchkarma.dto.UserResponse.from(updatedAdmin);
    }

    public void deleteAdmin(Long id) {
        if (!adminRepository.existsById(id)) {
            throw new IllegalArgumentException("Admin not found with id: " + id);
        }
        adminRepository.deleteById(id);
    }

    public void updateLastLogin(Long id) {
        Admin admin = getAdminEntityById(id);
        admin.setLastLogin(LocalDateTime.now());
        adminRepository.save(admin);
    }
}