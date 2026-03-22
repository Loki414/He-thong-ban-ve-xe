package com.example.busticket.controller;

import com.example.busticket.dto.DailyBookingResponse;
import com.example.busticket.dto.RevenueTodayResponse;
import com.example.busticket.dto.TicketResponse;
import com.example.busticket.dto.TripStatisticResponse;
import com.example.busticket.service.AdminService;
import com.example.busticket.service.TicketService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final TicketService ticketService;

    public AdminController(AdminService adminService, TicketService ticketService) {
        this.adminService = adminService;
        this.ticketService = ticketService;
    }

    /** All tickets — same data as GET /api/tickets; exposed under /api/admin for dashboard consistency. */
    @GetMapping("/tickets")
    public List<TicketResponse> listAllTickets() {
        return ticketService.getAll();
    }

    @GetMapping("/revenue/today")
    public RevenueTodayResponse getRevenueToday() {
        return adminService.getRevenueToday();
    }

    @GetMapping("/bookings/last30days")
    public List<DailyBookingResponse> getBookingsLast30Days() {
        return adminService.getBookingsLast30Days();
    }

    @GetMapping("/trips/statistics")
    public List<TripStatisticResponse> getTripStatistics() {
        return adminService.getTripStatistics();
    }
}

