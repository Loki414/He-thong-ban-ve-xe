package com.example.busticket.service;

import com.example.busticket.dto.AuthLoginRequest;
import com.example.busticket.dto.AuthRegisterRequest;
import com.example.busticket.dto.AuthResponse;
import com.example.busticket.entity.AuthProvider;
import com.example.busticket.entity.PasswordResetToken;
import com.example.busticket.entity.User;
import com.example.busticket.entity.UserRole;
import com.example.busticket.repository.PasswordResetTokenRepository;
import com.example.busticket.repository.UserRepository;
import com.example.busticket.security.JwtService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GoogleOAuthService googleOAuthService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${auth.password-reset.log-otp:true}")
    private boolean logPasswordResetOtp;

    @Value("${auth.password-reset.ttl-minutes:15}")
    private long passwordResetTtlMinutes;

    public AuthService(UserRepository userRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       GoogleOAuthService googleOAuthService) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.googleOAuthService = googleOAuthService;
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
        user.setAuthProvider(AuthProvider.LOCAL);

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

        String usernameVal = (request.username() != null) ? request.username().trim() : "";
        String emailVal = (request.email() != null) ? request.email().trim() : "";

        if (usernameVal.isEmpty() && emailVal.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Username or email is required");
        }

        User user = null;

        if (!usernameVal.isEmpty()) {
            user = userRepository.findByUsername(usernameVal).orElse(null);
        }

        if (user == null && !emailVal.isEmpty()) {
            user = userRepository.findByEmail(emailVal).orElse(null);
        }

        if (user == null) {
            String identity = usernameVal.isEmpty() ? emailVal : usernameVal;
            user = userRepository.findByUsernameOrEmail(identity, identity).orElse(null);
        }

        if (user == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "Tài khoản không tồn tại");
        }

        if (user.getAuthProvider() == AuthProvider.GOOGLE) {
            throw new ResponseStatusException(UNAUTHORIZED, "Tài khoản này đăng nhập bằng Google.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ResponseStatusException(UNAUTHORIZED, "Mật khẩu không đúng");
        }

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(user.getRole().name()))
        );

        return new AuthResponse(
                jwtService.generateToken(userDetails, Boolean.TRUE.equals(request.rememberMe())),
                user.getId(),
                user.getUsername(),
                user.getRole().name()
        );
    }

    public AuthResponse loginWithGoogle(String idToken) {
        GoogleIdToken.Payload payload = googleOAuthService.verifyIdToken(idToken);
        String email = payload.getEmail();
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(UNAUTHORIZED, "Google không cung cấp email.");
        }
        email = email.trim().toLowerCase();

        var existing = userRepository.findByEmail(email);
        if (existing.isPresent()) {
            User user = existing.get();
            if (user.getAuthProvider() == AuthProvider.LOCAL) {
                throw new ResponseStatusException(CONFLICT,
                        "Email này đã đăng ký bằng mật khẩu. Vui lòng đăng nhập thường.");
            }
            return buildAuthResponse(user);
        }

        String baseUsername = deriveBaseUsername(email);
        String username = ensureUniqueUsername(baseUsername);
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID() + UUID.randomUUID().toString()));
        user.setRole(UserRole.ROLE_USER);
        user.setAuthProvider(AuthProvider.GOOGLE);
        User saved = userRepository.save(user);
        return buildAuthResponse(saved);
    }

    private AuthResponse buildAuthResponse(User user) {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(user.getRole().name()))
        );
        return new AuthResponse(
                jwtService.generateToken(userDetails, true),
                user.getId(),
                user.getUsername(),
                user.getRole().name()
        );
    }

    private String deriveBaseUsername(String email) {
        int at = email.indexOf('@');
        String local = at > 0 ? email.substring(0, at) : email;
        local = local.replaceAll("[^a-zA-Z0-9_]", "");
        if (local.length() < 3) {
            local = "user";
        }
        if (local.length() > 40) {
            local = local.substring(0, 40);
        }
        return local;
    }

    private String ensureUniqueUsername(String base) {
        String candidate = base;
        int i = 0;
        while (userRepository.existsByUsername(candidate)) {
            i++;
            String suffix = "_" + i;
            int max = 50 - suffix.length();
            String prefix = base.length() > max ? base.substring(0, max) : base;
            candidate = prefix + suffix;
        }
        return candidate;
    }

    public void requestPasswordReset(String emailRaw) {
        String email = emailRaw != null ? emailRaw.trim().toLowerCase() : "";
        if (email.isBlank()) {
            return;
        }
        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.getAuthProvider() == AuthProvider.GOOGLE) {
                return;
            }
            passwordResetTokenRepository.deleteByUser(user);
            String code = String.format("%06d", secureRandom.nextInt(1_000_000));
            PasswordResetToken token = new PasswordResetToken();
            token.setUser(user);
            token.setCode(code);
            token.setExpiresAt(Instant.now().plus(passwordResetTtlMinutes, ChronoUnit.MINUTES));
            token.setConsumed(false);
            passwordResetTokenRepository.save(token);
            if (logPasswordResetOtp) {
                log.info("Password reset OTP for {} : {} (valid {} min)", email, code, passwordResetTtlMinutes);
            }
        });
    }

    public void resetPassword(String emailRaw, String codeRaw, String newPassword) {
        String email = emailRaw != null ? emailRaw.trim().toLowerCase() : "";
        String code = codeRaw != null ? codeRaw.trim() : "";
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Mã hoặc email không hợp lệ."));
        if (user.getAuthProvider() == AuthProvider.GOOGLE) {
            throw new ResponseStatusException(BAD_REQUEST, "Tài khoản này đăng nhập bằng Google.");
        }
        PasswordResetToken token = passwordResetTokenRepository
                .findByUserAndCodeAndConsumedFalse(user, code)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Mã hoặc email không hợp lệ."));
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(BAD_REQUEST, "Mã đã hết hạn. Vui lòng yêu cầu gửi lại.");
        }
        token.setConsumed(true);
        user.setPassword(passwordEncoder.encode(newPassword));
        passwordResetTokenRepository.save(token);
        userRepository.save(user);
    }
}
