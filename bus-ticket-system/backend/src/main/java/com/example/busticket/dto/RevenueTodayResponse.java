package com.example.busticket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/** Expose as "revenue" for admin dashboard KPI. */
public record RevenueTodayResponse(@JsonProperty("revenue") BigDecimal amount) {
}

