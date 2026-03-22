package com.example.busticket.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TripResponse(
        Long id,
        BusResponse bus,
        RouteResponse route,
        LocalDateTime departureTime,
        BigDecimal price,
        int totalSeats,
        int availableSeats
) {
}

