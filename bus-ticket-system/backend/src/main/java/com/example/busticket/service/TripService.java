package com.example.busticket.service;

import com.example.busticket.dto.TripRequest;
import com.example.busticket.dto.TripResponse;
import com.example.busticket.entity.Bus;
import com.example.busticket.entity.Route;
import com.example.busticket.entity.Seat;
import com.example.busticket.entity.Trip;
import com.example.busticket.repository.BusRepository;
import com.example.busticket.repository.RouteRepository;
import com.example.busticket.repository.SeatRepository;
import com.example.busticket.repository.TripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class TripService {

    private final TripRepository tripRepository;
    private final BusRepository busRepository;
    private final RouteRepository routeRepository;
    private final SeatRepository seatRepository;
    private final MapperService mapperService;

    public TripService(TripRepository tripRepository,
                       BusRepository busRepository,
                       RouteRepository routeRepository,
                       SeatRepository seatRepository,
                       MapperService mapperService) {
        this.tripRepository = tripRepository;
        this.busRepository = busRepository;
        this.routeRepository = routeRepository;
        this.seatRepository = seatRepository;
        this.mapperService = mapperService;
    }

    public List<TripResponse> getAll() {
        return tripRepository.findAll().stream().map(mapperService::toTripResponse).toList();
    }

    public List<TripResponse> search(String origin, String destination) {
        String safeOrigin = origin == null ? "" : origin.trim();
        String safeDestination = destination == null ? "" : destination.trim();
        return tripRepository.findByRouteOriginIgnoreCaseContainingAndRouteDestinationIgnoreCaseContaining(safeOrigin, safeDestination)
                .stream()
                .map(mapperService::toTripResponse)
                .toList();
    }

    @Transactional
    public TripResponse create(TripRequest request) {
        Bus bus = busRepository.findById(request.busId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Bus not found"));
        Route route = routeRepository.findById(request.routeId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Route not found"));

        Trip trip = new Trip();
        trip.setBus(bus);
        trip.setRoute(route);
        trip.setDepartureTime(request.departureTime());
        trip.setPrice(request.price());

        Trip savedTrip = tripRepository.save(trip);
        seedSeatsForTrip(savedTrip, bus.getTotalSeats());
        return mapperService.toTripResponse(savedTrip);
    }

    private void seedSeatsForTrip(Trip trip, int totalSeats) {
        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= totalSeats; i++) {
            Seat seat = new Seat();
            seat.setTrip(trip);
            seat.setSeatNumber(String.format("A%02d", i));
            seat.setBooked(false);
            seats.add(seat);
        }
        seatRepository.saveAll(seats);
    }
}

