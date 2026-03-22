package com.example.busticket.controller;

import com.example.busticket.dto.RouteRequest;
import com.example.busticket.dto.RouteResponse;
import com.example.busticket.service.RouteService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    public List<RouteResponse> getAll() {
        return routeService.getAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public RouteResponse create(@Valid @RequestBody RouteRequest request) {
        return routeService.create(request);
    }
}

