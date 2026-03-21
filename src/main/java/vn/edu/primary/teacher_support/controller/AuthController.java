package vn.edu.primary.teacher_support.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.primary.teacher_support.dto.LoginRequest;
import vn.edu.primary.teacher_support.dto.OtpRequest;
import vn.edu.primary.teacher_support.dto.RegisterRequest;
import vn.edu.primary.teacher_support.entity.User;
import vn.edu.primary.teacher_support.service.AuthService;
import vn.edu.primary.teacher_support.service.OtpService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final OtpService  otpService;

    public AuthController(AuthService authService, OtpService otpService) {
        this.authService = authService;
        this.otpService  = otpService;
    }

    //  GỬI OTP
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody OtpRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body("Email không được để trống");
        }
        otpService.sendOtp(request.getEmail().trim());
        return ResponseEntity.ok("OTP sent");
    }

    // XÁC THỰC OTP
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpRequest request) {
        boolean valid = otpService.verifyOtp(
                request.getEmail().trim(),
                request.getOtp().trim()
        );
        if (!valid) {
            return ResponseEntity.badRequest().body("OTP không đúng hoặc đã hết hạn");
        }
        return ResponseEntity.ok("OTP verified");
    }

    //  ĐĂNG KÝ
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        User newUser = authService.register(request);
        return ResponseEntity.ok("Register success");
    }

    // ĐĂNG NHẬP
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String result = authService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(result);
    }
}