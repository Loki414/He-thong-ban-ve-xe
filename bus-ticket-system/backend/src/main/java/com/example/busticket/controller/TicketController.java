package com.example.busticket.controller;

import com.example.busticket.dto.TicketRequest;
import com.example.busticket.dto.TicketResponse;
import com.example.busticket.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<TicketResponse> getAll() {
        return ticketService.getAll();
    }

    @PostMapping
    public TicketResponse create(@Valid @RequestBody TicketRequest request) {
        return ticketService.create(request);
    }
}

