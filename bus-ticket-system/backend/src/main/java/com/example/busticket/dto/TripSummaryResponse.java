package com.example.busticket.dto;

import java.time.LocalDateTime;

/**
 * Trip info embedded in ticket list (no seat counts — avoids extra queries / heavy nesting).
 */
public record TripSummaryResponse(
        Long id,
        BusResponse bus,
        RouteResponse route,
        LocalDateTime departureTime
) {
}
