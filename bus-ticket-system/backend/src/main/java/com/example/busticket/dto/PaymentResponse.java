package com.example.busticket.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long ticketId,
        BigDecimal amount,
        String paymentMethod,
        String status,
        LocalDateTime paymentTime
) {
}

