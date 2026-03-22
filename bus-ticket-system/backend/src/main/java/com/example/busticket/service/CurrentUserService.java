package com.example.busticket.service;

import com.example.busticket.entity.User;
import com.example.busticket.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "Unauthorized");
        }

        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Unauthorized"));
    }

    public void validateRequestedUser(Long userId) {
        User currentUser = getCurrentUser();
        if (!currentUser.getRole().name().equals("ROLE_ADMIN") && !currentUser.getId().equals(userId)) {
            throw new ResponseStatusException(FORBIDDEN, "You can only perform this action for your own account");
        }
    }

    public void validateTicketAccess(User ticketOwner) {
        User currentUser = getCurrentUser();
        if (!currentUser.getRole().name().equals("ROLE_ADMIN") && !currentUser.getId().equals(ticketOwner.getId())) {
            throw new ResponseStatusException(FORBIDDEN, "You do not have permission to pay for this ticket");
        }
    }
}

