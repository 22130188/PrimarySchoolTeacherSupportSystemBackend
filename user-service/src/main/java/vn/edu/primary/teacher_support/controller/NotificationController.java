package vn.edu.primary.teacher_support.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.primary.teacher_support.dto.NotificationResponse;
import vn.edu.primary.teacher_support.entity.User;
import vn.edu.primary.teacher_support.repository.UserRepository;
import vn.edu.primary.teacher_support.service.JwtService;
import vn.edu.primary.teacher_support.service.NotificationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "50") int limit) {
        User user = authenticate(authorization);
        return ResponseEntity.ok(notificationService.getNotifications(user.getId(), limit));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @RequestHeader("Authorization") String authorization) {
        User user = authenticate(authorization);
        return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount(user.getId())));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markRead(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        User user = authenticate(authorization);
        return ResponseEntity.ok(notificationService.markRead(id, user.getId()));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Integer>> markAllRead(
            @RequestHeader("Authorization") String authorization) {
        User user = authenticate(authorization);
        return ResponseEntity.ok(Map.of("updated", notificationService.markAllRead(user.getId())));
    }

    private User authenticate(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new RuntimeException("Thiếu token xác thực");
        }
        String token = authorization.substring(7);
        if (!jwtService.isValid(token)) throw new RuntimeException("Token không hợp lệ hoặc đã hết hạn");
        return userRepository.findByUsername(jwtService.extractUsername(token))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
    }
}
