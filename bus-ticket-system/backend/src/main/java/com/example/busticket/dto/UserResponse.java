package com.example.busticket.dto;

public record UserResponse(
        Long id,
        String username,
        String email,
        String role
) {
}

