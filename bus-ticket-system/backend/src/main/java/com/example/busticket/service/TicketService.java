package com.example.busticket.service;

import com.example.busticket.dto.TicketRequest;
import com.example.busticket.dto.TicketResponse;
import com.example.busticket.entity.Seat;
import com.example.busticket.entity.SeatLock;
import com.example.busticket.entity.Ticket;
import com.example.busticket.entity.TicketStatus;
import com.example.busticket.entity.User;
import com.example.busticket.repository.SeatLockRepository;
import com.example.busticket.repository.SeatRepository;
import com.example.busticket.repository.TicketRepository;
import com.example.busticket.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final SeatLockRepository seatLockRepository;
    private final SeatService seatService;
    private final CurrentUserService currentUserService;
    private final MapperService mapperService;

    public TicketService(TicketRepository ticketRepository,
                         SeatRepository seatRepository,
                         UserRepository userRepository,
                         SeatLockRepository seatLockRepository,
                         SeatService seatService,
                         CurrentUserService currentUserService,
                         MapperService mapperService) {
        this.ticketRepository = ticketRepository;
        this.seatRepository = seatRepository;
        this.userRepository = userRepository;
        this.seatLockRepository = seatLockRepository;
        this.seatService = seatService;
        this.currentUserService = currentUserService;
        this.mapperService = mapperService;
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getAll() {
        return ticketRepository.findAll().stream().map(mapperService::toTicketResponse).toList();
    }

    @Transactional
    public TicketResponse create(TicketRequest request) {
        seatService.cleanupExpiredLocks();
        currentUserService.validateRequestedUser(request.userId());

        Seat seat = seatRepository.findById(request.seatId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Seat not found"));
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        if (seat.isBooked()) {
            throw new ResponseStatusException(BAD_REQUEST, "Seat already booked");
        }

        SeatLock seatLock = seatLockRepository.findBySeatId(seat.getId())
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Seat is not locked"));

        if (seatLock.getExpiresAt().isBefore(LocalDateTime.now())) {
            seatLockRepository.delete(seatLock);
            throw new ResponseStatusException(BAD_REQUEST, "Seat lock expired");
        }

        if (!seatLock.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(BAD_REQUEST, "Seat is locked by another user");
        }

        if (ticketRepository.existsBySeatIdAndStatusIn(seat.getId(), List.of(TicketStatus.PENDING, TicketStatus.CONFIRMED))) {
            throw new ResponseStatusException(BAD_REQUEST, "Seat already has an active ticket");
        }

        BigDecimal tripPrice = seat.getTrip().getPrice();
        if (tripPrice == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Trip price is not set");
        }

        Ticket ticket = new Ticket();
        ticket.setSeat(seat);
        ticket.setUser(user);
        ticket.setBookingTime(LocalDateTime.now());
        ticket.setStatus(TicketStatus.PENDING);
        ticket.setPrice(tripPrice);

        return mapperService.toTicketResponse(ticketRepository.save(ticket));
    }
}

