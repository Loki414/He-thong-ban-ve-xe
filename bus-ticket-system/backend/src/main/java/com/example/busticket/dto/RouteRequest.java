package com.example.busticket.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record RouteRequest(
        @NotBlank String origin,
        @NotBlank String destination,
        @Min(1) Double distance
) {
}

