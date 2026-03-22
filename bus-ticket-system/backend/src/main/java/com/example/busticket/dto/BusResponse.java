package com.example.busticket.dto;

public record BusResponse(
        Long id,
        String busNumber,
        String busType,
        Integer totalSeats
) {
}

