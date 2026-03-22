package com.example.busticket.controller;

import com.example.busticket.dto.SeatLockRequest;
import com.example.busticket.dto.SeatLockResponse;
import com.example.busticket.dto.SeatResponse;
import com.example.busticket.service.SeatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @GetMapping("/trip/{tripId}")
    public List<SeatResponse> getByTrip(@PathVariable("tripId") Long tripId) {
        return seatService.getSeatsByTrip(tripId);
    }

    @PostMapping("/lock")
    public SeatLockResponse lockSeat(@Valid @RequestBody SeatLockRequest request) {
        return seatService.lockSeat(request);
    }
}

