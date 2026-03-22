package com.example.busticket.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthLoginRequest(
        String username,
        String email,
        @NotBlank String password
) {
}

