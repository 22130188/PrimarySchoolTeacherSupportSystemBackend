package vn.edu.primary.teacher_support.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class AuthController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello";
    }

    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@RequestBody OtpRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body("Email không được để trống");
        }
        // TODO: thực hiện gửi OTP thật (hoặc gọi service hiện có nếu đã chuyển về module)
        return ResponseEntity.ok("OTP sent to " + request.getEmail().trim());
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody OtpRequest request) {
        if (request == null || request.getEmail() == null || request.getOtp() == null) {
            return ResponseEntity.badRequest().body("Dữ liệu không hợp lệ");
        }
        return ResponseEntity.ok("OTP verified (fake)");
    }

    public static class OtpRequest {
        private String email;
        private String otp;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getOtp() {
            return otp;
        }

        public void setOtp(String otp) {
            this.otp = otp;
        }
    }
}
