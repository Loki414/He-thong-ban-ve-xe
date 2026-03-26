package com.example.busticket.controller;

import com.example.busticket.dto.AuthLoginRequest;
import com.example.busticket.dto.AuthRegisterRequest;
import com.example.busticket.dto.AuthResponse;
import com.example.busticket.dto.ForgotPasswordRequest;
import com.example.busticket.dto.GoogleAuthRequest;
import com.example.busticket.dto.MessageResponse;
import com.example.busticket.dto.ResetPasswordRequest;
import com.example.busticket.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody AuthRegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthLoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/google")
    public AuthResponse google(@Valid @RequestBody GoogleAuthRequest request) {
        return authService.loginWithGoogle(request.idToken());
    }

    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.requestPasswordReset(request.email());
        return new MessageResponse(
                "Nếu email tồn tại và tài khoản dùng mật khẩu, mã OTP đã được tạo. Kiểm tra log backend nếu chưa cấu hình gửi email.");
    }

    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.email(), request.code(), request.newPassword());
        return new MessageResponse("Đổi mật khẩu thành công. Bạn có thể đăng nhập.");
    }
}

