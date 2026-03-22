package com.example.busticket.service;

import com.example.busticket.dto.DailyBookingResponse;
import com.example.busticket.dto.RevenueTodayResponse;
import com.example.busticket.dto.TripStatisticResponse;
import com.example.busticket.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;

@Service
public class AdminService {

    private final PaymentRepository paymentRepository;
    private final EntityManager entityManager;

    public AdminService(PaymentRepository paymentRepository, EntityManager entityManager) {
        this.paymentRepository = paymentRepository;
        this.entityManager = entityManager;
    }

    public RevenueTodayResponse getRevenueToday() {
        BigDecimal amount = paymentRepository.getTodayRevenue();
        return new RevenueTodayResponse(amount == null ? BigDecimal.ZERO : amount);
    }

    public List<DailyBookingResponse> getBookingsLast30Days() {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery("""
                select date(booking_time) as booking_date, count(*) as total
                from tickets
                where booking_time >= curdate() - interval 29 day
                group by date(booking_time)
                order by booking_date
                """).getResultList();

        return rows.stream()
                .map(row -> new DailyBookingResponse(String.valueOf(row[0]), ((Number) row[1]).longValue()))
                .toList();
    }

    public List<TripStatisticResponse> getTripStatistics() {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery("""
                select concat(r.origin, ' - ', r.destination) as route_name, count(t.id) as total_trips
                from trips t
                join routes r on r.id = t.route_id
                group by r.id, r.origin, r.destination
                order by total_trips desc
                """).getResultList();

        return rows.stream()
                .map(row -> new TripStatisticResponse(String.valueOf(row[0]), ((Number) row[1]).longValue()))
                .toList();
    }
}

