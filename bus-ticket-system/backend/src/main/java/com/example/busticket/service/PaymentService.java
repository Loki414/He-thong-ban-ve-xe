package com.example.busticket.service;

import com.example.busticket.dto.PaymentRequest;
import com.example.busticket.dto.PaymentResponse;
import com.example.busticket.entity.Payment;
import com.example.busticket.entity.PaymentMethod;
import com.example.busticket.entity.PaymentStatus;
import com.example.busticket.entity.Ticket;
import com.example.busticket.entity.TicketStatus;
import com.example.busticket.repository.PaymentRepository;
import com.example.busticket.repository.SeatLockRepository;
import com.example.busticket.repository.SeatRepository;
import com.example.busticket.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;
    private final SeatRepository seatRepository;
    private final SeatLockRepository seatLockRepository;
    private final CurrentUserService currentUserService;
    private final MapperService mapperService;

    public PaymentService(PaymentRepository paymentRepository,
                          TicketRepository ticketRepository,
                          SeatRepository seatRepository,
                          SeatLockRepository seatLockRepository,
                          CurrentUserService currentUserService,
                          MapperService mapperService) {
        this.paymentRepository = paymentRepository;
        this.ticketRepository = ticketRepository;
        this.seatRepository = seatRepository;
        this.seatLockRepository = seatLockRepository;
        this.currentUserService = currentUserService;
        this.mapperService = mapperService;
    }

    public List<PaymentResponse> getAll() {
        return paymentRepository.findAll().stream().map(mapperService::toPaymentResponse).toList();
    }

    @Transactional
    public PaymentResponse process(PaymentRequest request) {
        Ticket ticket = ticketRepository.findById(request.ticketId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Ticket not found"));
        currentUserService.validateTicketAccess(ticket.getUser());

        if (paymentRepository.findByTicketId(ticket.getId()).isPresent()) {
            throw new ResponseStatusException(BAD_REQUEST, "Payment already exists for this ticket");
        }

        Payment payment = new Payment();
        payment.setTicket(ticket);
        payment.setAmount(request.amount());
        try {
            payment.setPaymentMethod(PaymentMethod.valueOf(request.method().toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(BAD_REQUEST, "Unsupported payment method");
        }
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaymentTime(LocalDateTime.now());

        ticket.setStatus(TicketStatus.CONFIRMED);
        ticket.getSeat().setBooked(true);
        ticketRepository.save(ticket);
        seatRepository.save(ticket.getSeat());
        seatLockRepository.deleteBySeatId(ticket.getSeat().getId());

        return mapperService.toPaymentResponse(paymentRepository.save(payment));
    }
}

