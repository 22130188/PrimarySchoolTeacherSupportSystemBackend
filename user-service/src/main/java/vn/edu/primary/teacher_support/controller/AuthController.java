package vn.edu.primary.teacher_support.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import vn.edu.primary.teacher_support.dto.LoginRequest;
import vn.edu.primary.teacher_support.dto.LoginResponse;
import vn.edu.primary.teacher_support.dto.OtpRequest;
import vn.edu.primary.teacher_support.dto.RegisterRequest;
import vn.edu.primary.teacher_support.entity.User;
import vn.edu.primary.teacher_support.service.AuthService;
import vn.edu.primary.teacher_support.service.OtpService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final OtpService  otpService;
    private final RestTemplate restTemplate;

    public AuthController(AuthService authService, OtpService otpService, RestTemplate restTemplate) {
        this.authService = authService;
        this.otpService  = otpService;
        this.restTemplate = restTemplate;
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
        User user = authService.register(request);

        // After successful registration, notify classroom-service
        // to resolve any WAITING_REGISTER invitations for this email
        resolveClassroomInvitations(user.getEmail(), user.getId());

        return ResponseEntity.ok("Register success");
    }

    // ĐĂNG NHẬP
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String[] loginResult = authService.login(request.getUsername(), request.getPassword());
        String token = loginResult[0];
        Integer roleId = Integer.parseInt(loginResult[1]);
        String roleName = loginResult[2];
        return ResponseEntity.ok(new LoginResponse(token, roleId, roleName));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }

    /**
     * Call classroom-service to resolve WAITING_REGISTER invitations
     * after a user successfully registers.
     * This is fire-and-forget — failure should not block registration.
     */
    private void resolveClassroomInvitations(String email, Long userId) {
        try {
            String url = "http://classroom-service/api/internal/invitations/resolve-after-register";
            Map<String, Object> body = Map.of("email", email, "userId", userId);
            restTemplate.postForEntity(url, body, Map.class);
            log.info("Resolved classroom invitations for email={}, userId={}", email, userId);
        } catch (Exception e) {
            // Don't fail registration if classroom-service is down
            log.warn("Could not resolve classroom invitations for {}: {}", email, e.getMessage());
        }
    }
}