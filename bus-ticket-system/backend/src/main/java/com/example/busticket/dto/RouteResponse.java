package com.example.busticket.dto;

public record RouteResponse(
        Long id,
        String origin,
        String destination,
        Double distance
) {
}

