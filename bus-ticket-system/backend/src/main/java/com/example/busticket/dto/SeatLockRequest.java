package com.example.busticket.dto;

import jakarta.validation.constraints.NotNull;

public record SeatLockRequest(
        @NotNull Long seatId,
        @NotNull Long userId
) {
}

