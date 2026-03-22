package com.example.busticket.controller;

import com.example.busticket.dto.TripRequest;
import com.example.busticket.dto.TripResponse;
import com.example.busticket.service.TripService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @GetMapping
    public List<TripResponse> getAll() {
        return tripService.getAll();
    }

    @GetMapping("/search")
    public List<TripResponse> search(@RequestParam("origin") String origin, @RequestParam("destination") String destination) {
        return tripService.search(origin, destination);
    }

    /** Same as /search; used by trips page (realtime). No server-side time filter. */
    @GetMapping("/realtime")
    public List<TripResponse> realtime(@RequestParam("origin") String origin, @RequestParam("destination") String destination) {
        return tripService.search(origin, destination);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public TripResponse create(@Valid @RequestBody TripRequest request) {
        return tripService.create(request);
    }
}

