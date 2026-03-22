package com.example.busticket.service;

import com.example.busticket.dto.SeatLockRequest;
import com.example.busticket.dto.SeatLockResponse;
import com.example.busticket.dto.SeatResponse;
import com.example.busticket.entity.Seat;
import com.example.busticket.entity.SeatLock;
import com.example.busticket.entity.User;
import com.example.busticket.repository.SeatLockRepository;
import com.example.busticket.repository.SeatRepository;
import com.example.busticket.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class SeatService {

    private static final long LOCK_MINUTES = 5;

    private final SeatRepository seatRepository;
    private final SeatLockRepository seatLockRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final MapperService mapperService;

    public SeatService(SeatRepository seatRepository,
                       SeatLockRepository seatLockRepository,
                       UserRepository userRepository,
                       CurrentUserService currentUserService,
                       MapperService mapperService) {
        this.seatRepository = seatRepository;
        this.seatLockRepository = seatLockRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.mapperService = mapperService;
    }

    @Transactional(readOnly = true)
    public List<SeatResponse> getSeatsByTrip(Long tripId) {
        // Khong goi cleanupExpiredLocks() o day (read-only); @Scheduled da xu ly
        LocalDateTime now = LocalDateTime.now();
        Map<Long, SeatLock> activeLocks = seatLockRepository.findActiveLocksForTrip(tripId, now)
                .stream()
                .collect(Collectors.toMap(lock -> lock.getSeat().getId(), Function.identity()));

        return seatRepository.findByTripIdOrderByIdAsc(tripId)
                .stream()
                .map(seat -> mapperService.toSeatResponse(seat, seat.isBooked() || activeLocks.containsKey(seat.getId())))
                .toList();
    }

    @Transactional
    public SeatLockResponse lockSeat(SeatLockRequest request) {
        cleanupExpiredLocks();
        currentUserService.validateRequestedUser(request.userId());

        Seat seat = seatRepository.findById(request.seatId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Seat not found"));
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        if (seat.isBooked()) {
            throw new ResponseStatusException(BAD_REQUEST, "Seat already booked");
        }

        LocalDateTime now = LocalDateTime.now();
        SeatLock seatLock = seatLockRepository.findBySeatId(seat.getId()).orElse(null);

        if (seatLock != null && seatLock.getExpiresAt().isAfter(now) && !seatLock.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(BAD_REQUEST, "Seat is temporarily locked by another user");
        }

        if (seatLock == null) {
            seatLock = new SeatLock();
            seatLock.setSeat(seat);
        }

        seatLock.setUser(user);
        seatLock.setLockedAt(now);
        seatLock.setExpiresAt(now.plusMinutes(LOCK_MINUTES));
        SeatLock saved = seatLockRepository.save(seatLock);

        return new SeatLockResponse(saved.getSeat().getId(), saved.getSeat().getSeatNumber(), saved.getExpiresAt(), "Seat locked for 5 minutes");
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void cleanupExpiredLocks() {
        List<SeatLock> expiredLocks = seatLockRepository.findByExpiresAtBefore(LocalDateTime.now());
        if (!expiredLocks.isEmpty()) {
            seatLockRepository.deleteAll(expiredLocks);
        }
    }
}

