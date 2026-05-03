package com.ayursutra.panchkarma.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleAuthRequest {

    @NotBlank(message = "Google token is required")
    private String googleToken;
}