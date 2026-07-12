package vn.edu.primary.teacher_support.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.primary.teacher_support.entity.AccessLog;
import vn.edu.primary.teacher_support.entity.Role;
import vn.edu.primary.teacher_support.entity.User;
import vn.edu.primary.teacher_support.repository.AccessLogRepository;
import vn.edu.primary.teacher_support.repository.UserRepository;
import vn.edu.primary.teacher_support.service.JwtService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/access-logs")
@RequiredArgsConstructor
public class AccessLogController {
    private final AccessLogRepository accessLogRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @GetMapping
    public List<AccessLog> getAccessLogs(@RequestHeader("Authorization") String authorization) {
        authenticateAdmin(authorization);
        return accessLogRepository.findTop500ByOrderByCreatedAtDesc();
    }

    private void authenticateAdmin(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) throw new RuntimeException("Thiếu token xác thực");
        String token = authorization.substring(7);
        if (!jwtService.isValid(token)) throw new RuntimeException("Token không hợp lệ hoặc đã hết hạn");
        User user = userRepository.findByUsername(jwtService.extractUsername(token))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        if (user.getRole() != Role.RoleName.ADMIN && user.getRoles().stream().noneMatch(r -> r.getName() == Role.RoleName.ADMIN)) {
            throw new RuntimeException("Bạn không có quyền xem nhật ký truy cập");
        }
    }
}
