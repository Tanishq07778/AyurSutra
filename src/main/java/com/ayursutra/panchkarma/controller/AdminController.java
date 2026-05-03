package com.ayursutra.panchkarma.controller;

import com.ayursutra.panchkarma.dto.AdminRegistrationRequest;
import com.ayursutra.panchkarma.dto.ApiResponse;
import com.ayursutra.panchkarma.dto.UserResponse;
import com.ayursutra.panchkarma.entity.Admin;
import com.ayursutra.panchkarma.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admins")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * Register a new admin
     * POST /api/v1/admins/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> registerAdmin(
            @Valid @RequestBody AdminRegistrationRequest request) {

        UserResponse admin = adminService.registerAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Admin registered successfully", admin));
    }

    /**
     * Get admin by ID
     * GET /api/v1/admins/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getAdminById(@PathVariable Long id) {
        UserResponse admin = adminService.getAdminById(id);
        return ResponseEntity.ok(ApiResponse.success("Admin found", admin));
    }

    /**
     * Get all admins
     * GET /api/v1/admins
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllAdmins() {
        List<UserResponse> admins = adminService.getAllAdmins();
        return ResponseEntity.ok(ApiResponse.success(
                "Retrieved " + admins.size() + " admins", admins));
    }

    /**
     * Update admin information
     * PUT /api/v1/admins/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateAdmin(
            @PathVariable Long id,
            @Valid @RequestBody AdminRegistrationRequest request) {

        UserResponse updatedAdmin = adminService.updateAdmin(id, request);
        return ResponseEntity.ok(ApiResponse.success("Admin updated successfully", updatedAdmin));
    }

    /**
     * Delete admin
     * DELETE /api/v1/admins/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAdmin(@PathVariable Long id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.ok(ApiResponse.success("Admin deleted successfully", null));
    }

    /**
     * Update admin's last login timestamp
     * PATCH /api/v1/admins/{id}/login
     */
    @PatchMapping("/{id}/login")
    public ResponseEntity<ApiResponse<Void>> updateLastLogin(@PathVariable Long id) {
        adminService.updateLastLogin(id);
        return ResponseEntity.ok(ApiResponse.success("Last login updated", null));
    }

    /**
     * Get admin's complete profile
     * GET /api/v1/admins/{id}/profile
     */
    @GetMapping("/{id}/profile")
    public ResponseEntity<ApiResponse<Admin>> getAdminProfile(@PathVariable Long id) {
        Admin admin = adminService.getAdminEntityById(id);
        return ResponseEntity.ok(ApiResponse.success("Admin profile retrieved", admin));
    }
}