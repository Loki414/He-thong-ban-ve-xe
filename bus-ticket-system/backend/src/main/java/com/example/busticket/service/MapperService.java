package com.example.busticket.service;

import com.example.busticket.dto.*;
import com.example.busticket.entity.*;
import com.example.busticket.repository.SeatRepository;
import org.springframework.stereotype.Service;

@Service
public class MapperService {

    private final SeatRepository seatRepository;

    public MapperService(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    public BusResponse toBusResponse(Bus bus) {
        return new BusResponse(bus.getId(), bus.getBusNumber(), bus.getBusType(), bus.getTotalSeats());
    }

    public RouteResponse toRouteResponse(Route route) {
        return new RouteResponse(route.getId(), route.getOrigin(), route.getDestination(), route.getDistance());
    }

    public TripResponse toTripResponse(Trip trip) {
        long total = seatRepository.countByTripId(trip.getId());
        long available = seatRepository.countByTripIdAndBookedFalse(trip.getId());
        return new TripResponse(
                trip.getId(),
                toBusResponse(trip.getBus()),
                toRouteResponse(trip.getRoute()),
                trip.getDepartureTime(),
                trip.getPrice(),
                (int) Math.min(total, Integer.MAX_VALUE),
                (int) Math.min(available, Integer.MAX_VALUE)
        );
    }

    public TripSummaryResponse toTripSummaryResponse(Trip trip) {
        return new TripSummaryResponse(
                trip.getId(),
                toBusResponse(trip.getBus()),
                toRouteResponse(trip.getRoute()),
                trip.getDepartureTime()
        );
    }

    public SeatResponse toSeatResponse(Seat seat, boolean booked) {
        return new SeatResponse(seat.getId(), seat.getSeatNumber(), booked);
    }

    public UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole().name());
    }

    public TicketResponse toTicketResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                toSeatResponse(ticket.getSeat(), ticket.getSeat().isBooked()),
                toUserResponse(ticket.getUser()),
                ticket.getBookingTime(),
                ticket.getPrice(),
                ticket.getStatus().name(),
                toTripSummaryResponse(ticket.getSeat().getTrip())
        );
    }

    public PaymentResponse toPaymentResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getTicket().getId(),
                payment.getAmount(),
                payment.getPaymentMethod().name(),
                payment.getStatus().name(),
                payment.getPaymentTime()
        );
    }
}

