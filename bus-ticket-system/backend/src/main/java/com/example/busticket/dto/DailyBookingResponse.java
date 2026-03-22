package com.example.busticket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/** JSON uses "count" for chart (see admin dashboard). */
public record DailyBookingResponse(
        String date,
        @JsonProperty("count") long bookings
) {
}

