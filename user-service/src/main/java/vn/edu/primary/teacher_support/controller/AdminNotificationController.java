package vn.edu.primary.teacher_support.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.primary.teacher_support.dto.NotificationBroadcastRequest;
import vn.edu.primary.teacher_support.entity.Role;
import vn.edu.primary.teacher_support.entity.User;
import vn.edu.primary.teacher_support.repository.UserRepository;
import vn.edu.primary.teacher_support.service.JwtService;
import vn.edu.primary.teacher_support.service.NotificationService;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final NotificationService notificationService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @PostMapping("/broadcast")
    public ResponseEntity<Map<String, Integer>> broadcast(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody NotificationBroadcastRequest request) {
        User admin = authenticateAdmin(authorization);
        return ResponseEntity.ok(Map.of("sent", notificationService.broadcast(request, admin)));
    }

    private User authenticateAdmin(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new RuntimeException("Thiếu token xác thực");
        }
        String token = authorization.substring(7);
        if (!jwtService.isValid(token)) throw new RuntimeException("Token không hợp lệ hoặc đã hết hạn");
        User user = userRepository.findByUsername(jwtService.extractUsername(token))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        if (user.getRole() != Role.RoleName.ADMIN && user.getRoles().stream().noneMatch(r -> r.getName() == Role.RoleName.ADMIN)) throw new RuntimeException("Bạn không có quyền gửi thông báo");
        return user;
    }
}
