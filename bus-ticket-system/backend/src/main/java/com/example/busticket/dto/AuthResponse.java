package com.example.busticket.dto;

public record AuthResponse(
        String token,
        Long userId,
        String username,
        String role
) {
}

