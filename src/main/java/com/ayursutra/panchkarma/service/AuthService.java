package com.ayursutra.panchkarma.service;

import com.ayursutra.panchkarma.config.JwtUtil;
import com.ayursutra.panchkarma.dto.AuthResponse;
import com.ayursutra.panchkarma.dto.LoginRequest;
import com.ayursutra.panchkarma.dto.UserResponse;
import com.ayursutra.panchkarma.entity.User;
import com.ayursutra.panchkarma.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        if (!user.isActive()) {
            throw new IllegalArgumentException("Account is not active");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());
        String refreshToken = generateRefreshToken();

        return new AuthResponse(token, refreshToken, UserResponse.from(user));
    }

    private String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }
}