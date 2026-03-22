package com.example.busticket.service;

import com.example.busticket.dto.AuthLoginRequest;
import com.example.busticket.dto.AuthRegisterRequest;
import com.example.busticket.dto.AuthResponse;
import com.example.busticket.entity.User;
import com.example.busticket.entity.UserRole;
import com.example.busticket.repository.UserRepository;
import com.example.busticket.security.JwtService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(AuthRegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(BAD_REQUEST, "Username already exists");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(BAD_REQUEST, "Email already exists");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.ROLE_USER);

        User saved = userRepository.save(user);
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                saved.getUsername(),
                saved.getPassword(),
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(saved.getRole().name()))
        );
        return new AuthResponse(jwtService.generateToken(userDetails), saved.getId(), saved.getUsername(), saved.getRole().name());
    }

    public AuthResponse login(AuthLoginRequest request) {
        if (request.password() == null || request.password().isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "Password is required");
        }

        // Try username first, then email
        String usernameVal = (request.username() != null) ? request.username().trim() : "";
        String emailVal    = (request.email()    != null) ? request.email().trim()    : "";

        if (usernameVal.isEmpty() && emailVal.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Username or email is required");
        }

        User user = null;

        // 1. Try exact username match
        if (!usernameVal.isEmpty()) {
            user = userRepository.findByUsername(usernameVal).orElse(null);
        }

        // 2. Try exact email match
        if (user == null && !emailVal.isEmpty()) {
            user = userRepository.findByEmail(emailVal).orElse(null);
        }

        // 3. Fallback: try the non-empty value as both username and email
        if (user == null) {
            String identity = usernameVal.isEmpty() ? emailVal : usernameVal;
            user = userRepository.findByUsernameOrEmail(identity, identity).orElse(null);
        }

        if (user == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "Tài khoản không tồn tại");
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ResponseStatusException(UNAUTHORIZED, "Mật khẩu không đúng");
        }

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(user.getRole().name()))
        );

        return new AuthResponse(jwtService.generateToken(userDetails), user.getId(), user.getUsername(), user.getRole().name());
    }
}

