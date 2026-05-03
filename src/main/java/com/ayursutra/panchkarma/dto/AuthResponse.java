package com.ayursutra.panchkarma.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String refreshToken;
    private String tokenType = "Bearer";
    private UserResponse user;

    public AuthResponse(String token, String refreshToken, UserResponse user) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.user = user;
    }
}