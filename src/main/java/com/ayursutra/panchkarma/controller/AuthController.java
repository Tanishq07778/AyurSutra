package com.ayursutra.panchkarma.controller;

import com.ayursutra.panchkarma.dto.*;
import com.ayursutra.panchkarma.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Login with email and password
     * POST /api/v1/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    /**
     * Test endpoint to check if token is valid
     * GET /api/v1/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<String>> getCurrentUser() {
        return ResponseEntity.ok(ApiResponse.success("Authenticated user", "User details here"));
    }
}
