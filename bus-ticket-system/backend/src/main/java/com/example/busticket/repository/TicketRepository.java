package com.example.busticket.repository;

import com.example.busticket.entity.Ticket;
import com.example.busticket.entity.TicketStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    long countByUser_Id(Long userId);

    @Override
    @EntityGraph(attributePaths = {"seat", "seat.trip", "seat.trip.bus", "seat.trip.route", "user"})
    List<Ticket> findAll();

    boolean existsBySeatIdAndStatusIn(Long seatId, Collection<TicketStatus> statuses);

    @EntityGraph(attributePaths = {"seat", "seat.trip", "seat.trip.bus", "seat.trip.route", "user"})
    Optional<Ticket> findById(Long id);

    @EntityGraph(attributePaths = {"seat", "seat.trip", "seat.trip.bus", "seat.trip.route", "user"})
    List<Ticket> findByUser_IdOrderByBookingTimeDesc(Long userId);
}

