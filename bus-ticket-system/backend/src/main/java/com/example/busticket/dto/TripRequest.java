package com.example.busticket.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TripRequest(
        @NotNull Long busId,
        @NotNull Long routeId,
        @NotNull LocalDateTime departureTime,
        @NotNull @DecimalMin("0.0") BigDecimal price
) {
}

