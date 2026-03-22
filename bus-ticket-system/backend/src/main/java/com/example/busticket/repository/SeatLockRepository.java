package com.example.busticket.repository;

import com.example.busticket.entity.SeatLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SeatLockRepository extends JpaRepository<SeatLock, Long> {
    Optional<SeatLock> findBySeatId(Long seatId);

    @Query("SELECT sl FROM SeatLock sl JOIN FETCH sl.seat s WHERE s.trip.id = :tripId AND sl.expiresAt > :now")
    List<SeatLock> findActiveLocksForTrip(@Param("tripId") Long tripId, @Param("now") LocalDateTime now);

    List<SeatLock> findByExpiresAtBefore(LocalDateTime now);
    void deleteBySeatId(Long seatId);
}

