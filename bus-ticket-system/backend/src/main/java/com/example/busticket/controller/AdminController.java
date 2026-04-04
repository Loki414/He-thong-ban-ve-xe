package com.example.busticket.controller;

import com.example.busticket.dto.DailyBookingResponse;
import com.example.busticket.dto.RevenueTodayResponse;
import com.example.busticket.dto.TicketResponse;
import com.example.busticket.dto.TripStatisticResponse;
import com.example.busticket.dto.UserCountResponse;
import com.example.busticket.dto.UserResponse;
import com.example.busticket.service.AdminService;
import com.example.busticket.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;

    public AdminController(AdminService adminService, UserService userService) {
        this.adminService = adminService;
        this.userService = userService;
    }

    /** Total registered accounts (same DB as đăng ký). */
    @GetMapping("/users/count")
    public UserCountResponse userCount() {
        return new UserCountResponse(adminService.countRegisteredUsers());
    }

    /** Alias without "users" in the path (some browsers/extensions block that segment). */
    @GetMapping("/accounts")
    public List<UserResponse> listAccounts() {
        return adminService.listRegisteredUsersForDashboard();
    }

    @DeleteMapping("/accounts/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /** User list — same data as /accounts (native SQL). */
    @GetMapping("/users")
    public List<UserResponse> listUsers() {
        return adminService.listRegisteredUsersForDashboard();
    }

    /** All tickets for admin UI — native SQL so list survives schema/JPA drift (GET /api/tickets unchanged). */
    @GetMapping("/tickets")
    public List<TicketResponse> listAllTickets() {
        return adminService.listTicketsForDashboard();
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

