package com.example.busticket.dto;

public record SeatResponse(
        Long id,
        String seatNumber,
        boolean booked
) {
}

