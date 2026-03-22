package com.example.busticket.repository;

import com.example.busticket.entity.Payment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Override
    @EntityGraph(attributePaths = {"ticket", "ticket.seat", "ticket.user"})
    List<Payment> findAll();

    Optional<Payment> findByTicketId(Long ticketId);

    @Query(value = """
            select coalesce(sum(amount), 0)
            from payments
            where status = 'SUCCESS' and date(payment_time) = curdate()
            """, nativeQuery = true)
    BigDecimal getTodayRevenue();
}

