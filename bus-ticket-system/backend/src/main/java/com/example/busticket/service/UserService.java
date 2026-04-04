package com.example.busticket.service;

import com.example.busticket.dto.UserResponse;
import com.example.busticket.entity.User;
import com.example.busticket.repository.PasswordResetTokenRepository;
import com.example.busticket.repository.SeatLockRepository;
import com.example.busticket.repository.TicketRepository;
import com.example.busticket.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final MapperService mapperService;
    private final CurrentUserService currentUserService;
    private final TicketRepository ticketRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final SeatLockRepository seatLockRepository;

    public UserService(UserRepository userRepository,
                       MapperService mapperService,
                       CurrentUserService currentUserService,
                       TicketRepository ticketRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       SeatLockRepository seatLockRepository) {
        this.userRepository = userRepository;
        this.mapperService = mapperService;
        this.currentUserService = currentUserService;
        this.ticketRepository = ticketRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.seatLockRepository = seatLockRepository;
    }

    public List<UserResponse> getAll() {
        return userRepository.findAll().stream().map(mapperService::toUserResponse).toList();
    }

    @Transactional
    public void deleteById(Long id) {
        User current = currentUserService.getCurrentUser();
        if (current.getId().equals(id)) {
            throw new ResponseStatusException(BAD_REQUEST, "Không thể xóa chính mình");
        }
        User target = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Không tìm thấy người dùng"));
        if (ticketRepository.countByUser_Id(id) > 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Không thể xóa: tài khoản còn vé trong hệ thống");
        }
        passwordResetTokenRepository.deleteByUser(target);
        seatLockRepository.deleteByUser_Id(id);
        userRepository.delete(target);
    }
}

