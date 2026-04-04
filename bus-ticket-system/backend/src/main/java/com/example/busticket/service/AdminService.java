package com.example.busticket.service;

import com.example.busticket.dto.BusResponse;
import com.example.busticket.dto.DailyBookingResponse;
import com.example.busticket.dto.RevenueTodayResponse;
import com.example.busticket.dto.RouteResponse;
import com.example.busticket.dto.SeatResponse;
import com.example.busticket.dto.TicketResponse;
import com.example.busticket.dto.TripStatisticResponse;
import com.example.busticket.dto.TripSummaryResponse;
import com.example.busticket.dto.UserResponse;
import com.example.busticket.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

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

    /** Native count so KPI matches DB even if listing users via JPA misbehaves. */
    public long countRegisteredUsers() {
        Object row = entityManager.createNativeQuery("select count(*) from users").getSingleResult();
        return row instanceof Number n ? n.longValue() : Long.parseLong(row.toString());
    }

    /**
     * Full user rows for admin table — native SQL so it stays in sync with {@link #countRegisteredUsers()}
     * even when JPA {@code findAll()} on {@code User} returns nothing (schema/Hibernate drift).
     */
    public List<UserResponse> listRegisteredUsersForDashboard() {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery("""
                select id, username, email, role from users order by id
                """).getResultList();
        return rows.stream().map(this::rowToUserResponse).toList();
    }

    private UserResponse rowToUserResponse(Object[] row) {
        return new UserResponse(
                ((Number) row[0]).longValue(),
                row[1] != null ? String.valueOf(row[1]) : "",
                row[2] != null ? String.valueOf(row[2]) : "",
                row[3] != null ? String.valueOf(row[3]) : "ROLE_USER"
        );
    }

    /**
     * Admin ticket table — native SQL + LEFT JOINs so broken FKs or JPA hydration issues
     * (common when DB schema drifts from entities) do not take down the whole list.
     */
    @Transactional(readOnly = true)
    public List<TicketResponse> listTicketsForDashboard() {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery("""
                select
                  t.id, t.booking_time, t.price, t.status,
                  s.id, s.seat_number, s.booked,
                  tr.id, tr.departure_time,
                  b.id, b.bus_number, b.bus_type, b.total_seats,
                  r.id, r.origin, r.destination, r.distance,
                  u.id, u.username, u.email, u.role
                from tickets t
                left join seats s on s.id = t.seat_id
                left join trips tr on tr.id = s.trip_id
                left join buses b on b.id = tr.bus_id
                left join routes r on r.id = tr.route_id
                left join users u on u.id = t.user_id
                order by t.id
                """).getResultList();

        return rows.stream().map(this::safeTicketRow).filter(r -> r != null).toList();
    }

    private TicketResponse safeTicketRow(Object[] row) {
        try {
            return ticketRowToResponse(row);
        } catch (RuntimeException e) {
            log.warn("Bỏ qua dòng vé (id={}): {}", row != null && row.length > 0 ? row[0] : "?", e.getMessage());
            return null;
        }
    }

    private TicketResponse ticketRowToResponse(Object[] row) {
        Long ticketId = longVal(row[0]);
        LocalDateTime bookingTime = toLocalDateTime(row[1]);
        BigDecimal price = toBigDecimal(row[2]);
        String status = row[3] != null ? String.valueOf(row[3]) : "UNKNOWN";

        Long seatId = longValOrZero(row[4]);
        String seatNumber = row[5] != null ? String.valueOf(row[5]) : "—";
        boolean seatBooked = boolVal(row[6]);
        SeatResponse seat = new SeatResponse(seatId, seatNumber, seatBooked);

        Long tripId = longValOrZero(row[7]);
        LocalDateTime departure = toLocalDateTime(row[8]);
        BusResponse bus = new BusResponse(
                longValOrZero(row[9]),
                row[10] != null ? String.valueOf(row[10]) : "—",
                row[11] != null ? String.valueOf(row[11]) : "—",
                intVal(row[12])
        );
        RouteResponse route = new RouteResponse(
                longValOrZero(row[13]),
                row[14] != null ? String.valueOf(row[14]) : "—",
                row[15] != null ? String.valueOf(row[15]) : "—",
                doubleVal(row[16])
        );
        TripSummaryResponse trip = new TripSummaryResponse(tripId, bus, route, departure);

        UserResponse user = new UserResponse(
                longValOrZero(row[17]),
                row[18] != null ? String.valueOf(row[18]) : "",
                row[19] != null ? String.valueOf(row[19]) : "",
                row[20] != null ? String.valueOf(row[20]) : "ROLE_USER"
        );

        return new TicketResponse(ticketId, seat, user, bookingTime, price, status, trip);
    }

    private static Long longVal(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Number n) {
            return n.longValue();
        }
        return Long.parseLong(o.toString());
    }

    private static long longValOrZero(Object o) {
        Long v = longVal(o);
        return v != null ? v : 0L;
    }

    private static int intVal(Object o) {
        if (o == null) {
            return 0;
        }
        if (o instanceof Number n) {
            return n.intValue();
        }
        return Integer.parseInt(o.toString());
    }

    private static Double doubleVal(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Number n) {
            return n.doubleValue();
        }
        return Double.parseDouble(o.toString());
    }

    private static BigDecimal toBigDecimal(Object o) {
        if (o == null) {
            return BigDecimal.ZERO;
        }
        if (o instanceof BigDecimal bd) {
            return bd;
        }
        if (o instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        return new BigDecimal(o.toString());
    }

    private static boolean boolVal(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof Boolean b) {
            return b;
        }
        if (o instanceof Number n) {
            return n.intValue() != 0;
        }
        return Boolean.parseBoolean(o.toString());
    }

    private static LocalDateTime toLocalDateTime(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof LocalDateTime ldt) {
            return ldt;
        }
        if (o instanceof java.sql.Timestamp ts) {
            return ts.toLocalDateTime();
        }
        if (o instanceof java.util.Date d) {
            return LocalDateTime.ofInstant(d.toInstant(), ZoneOffset.UTC);
        }
        return LocalDateTime.parse(o.toString());
    }
}

