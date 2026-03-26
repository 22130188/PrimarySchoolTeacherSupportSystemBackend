package vn.edu.primary.teacher_support.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.primary.teacher_support.dto.UserResponse;
import vn.edu.primary.teacher_support.entity.User;
import vn.edu.primary.teacher_support.repository.UserRepository;
import vn.edu.primary.teacher_support.service.JwtService;

@RestController
@RequestMapping("/api/user")
public class UserProfileController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public UserProfileController(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new RuntimeException("Thiếu token xác thực");
        }

        String token = authorization.substring(BEARER_PREFIX.length());
        if (!jwtService.isValid(token)) {
            throw new RuntimeException("Token không hợp lệ hoặc đã hết hạn");
        }

        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        return ResponseEntity.ok(UserResponse.from(user));
    }
}
