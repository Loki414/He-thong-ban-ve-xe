package com.example.busticket.dto;

import java.time.LocalDateTime;

public record SeatLockResponse(
        Long seatId,
        String seatNumber,
        LocalDateTime expiresAt,
        String message
) {
}

