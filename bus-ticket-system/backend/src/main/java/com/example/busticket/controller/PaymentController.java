package com.example.busticket.controller;

import com.example.busticket.dto.PaymentRequest;
import com.example.busticket.dto.PaymentResponse;
import com.example.busticket.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<PaymentResponse> getAll() {
        return paymentService.getAll();
    }

    @PostMapping
    public PaymentResponse process(@Valid @RequestBody PaymentRequest request) {
        return paymentService.process(request);
    }
}

