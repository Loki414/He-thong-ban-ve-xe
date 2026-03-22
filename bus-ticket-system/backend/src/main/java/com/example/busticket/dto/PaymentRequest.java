package com.example.busticket.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentRequest(
        @NotNull Long ticketId,
        @NotNull @DecimalMin("0.0") BigDecimal amount,
        @NotBlank String method
) {
}

