package com.example.busticket.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TicketResponse(
        Long id,
        SeatResponse seat,
        UserResponse user,
        LocalDateTime bookingTime,
        BigDecimal price,
        String status,
        TripSummaryResponse trip
) {
}

