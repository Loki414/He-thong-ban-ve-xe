package com.example.busticket.repository;

import com.example.busticket.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByTripIdOrderByIdAsc(Long tripId);

    long countByTripId(Long tripId);

    long countByTripIdAndBookedFalse(Long tripId);
}

