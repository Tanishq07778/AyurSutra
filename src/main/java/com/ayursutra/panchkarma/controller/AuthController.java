package com.ayursutra.panchkarma.controller;

import com.ayursutra.panchkarma.dto.*;
import com.ayursutra.panchkarma.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/v1/auth/login
     *
     * FIX: Old code returned AuthResponse which nested the user inside a .user field:
     *   { token, refreshToken, user: { id, email, role, firstName, ... } }
     *
     * The frontend read auth.id, auth.email, auth.role DIRECTLY from data.data,
     * but those fields are inside data.data.user — so id was always undefined,
     * localStorage stored { id: undefined } and Auth.isLoggedIn() returned false.
     *
     * FIX: Return a FLAT map that has BOTH the token fields AND all user fields
     * at the top level, so old and new frontend code both work:
     *   {
     *     token, refreshToken,
     *     id, email, role, firstName, lastName, patientId, doctorId,
     *     user: { ... same fields ... }   ← kept for backward compat
     *   }
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse authResponse = authService.login(request);
        UserResponse u = authResponse.getUser();

        // Build a flat map so frontend can read fields at any depth
        Map<String, Object> flat = new HashMap<>();

        // Token fields
        flat.put("token",        authResponse.getToken());
        flat.put("refreshToken", authResponse.getRefreshToken());
        flat.put("tokenType",    "Bearer");

        // User fields — FLAT (primary, what new code reads)
        flat.put("id",        u.getId());
        flat.put("email",     u.getEmail());
        flat.put("role",      u.getRole() != null ? u.getRole().name() : "PATIENT");
        flat.put("firstName", u.getFirstName());
        flat.put("lastName",  u.getLastName());
        flat.put("phone",     u.getPhone());
        flat.put("patientId", u.getPatientId());
        flat.put("doctorId",  u.getDoctorId());
        flat.put("profilePictureUrl", u.getProfilePictureUrl());

        // Also nest under "user" for any code that reads data.data.user
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id",        u.getId());
        userMap.put("email",     u.getEmail());
        userMap.put("role",      u.getRole() != null ? u.getRole().name() : "PATIENT");
        userMap.put("firstName", u.getFirstName());
        userMap.put("lastName",  u.getLastName());
        userMap.put("phone",     u.getPhone());
        userMap.put("patientId", u.getPatientId());
        userMap.put("doctorId",  u.getDoctorId());
        userMap.put("profilePictureUrl", u.getProfilePictureUrl());
        flat.put("user", userMap);

        return ResponseEntity.ok(ApiResponse.success("Login successful", flat));
    }

    /**
     * GET /api/v1/auth/me
     * Returns the currently authenticated user's email (token validation check).
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<String>> getCurrentUser(
            org.springframework.security.core.Authentication authentication) {
        String email = authentication != null ? authentication.getName() : "unknown";
        return ResponseEntity.ok(ApiResponse.success("Authenticated", email));
    }
}