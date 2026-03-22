package com.example.busticket.controller;

import com.example.busticket.dto.BusRequest;
import com.example.busticket.dto.BusResponse;
import com.example.busticket.service.BusService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buses")
public class BusController {

    private final BusService busService;

    public BusController(BusService busService) {
        this.busService = busService;
    }

    @GetMapping
    public List<BusResponse> getAll() {
        return busService.getAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public BusResponse create(@Valid @RequestBody BusRequest request) {
        return busService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public BusResponse update(@PathVariable("id") Long id, @Valid @RequestBody BusRequest request) {
        return busService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable("id") Long id) {
        busService.delete(id);
    }
}

