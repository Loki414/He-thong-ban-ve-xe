package com.example.busticket.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record BusRequest(
        @NotBlank String busNumber,
        @NotBlank String busType,
        @Min(1) Integer totalSeats
) {
}

