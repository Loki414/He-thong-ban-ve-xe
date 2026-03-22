package com.example.busticket.dto;

import jakarta.validation.constraints.NotNull;

public record TicketRequest(
        @NotNull Long seatId,
        @NotNull Long userId
) {
}

