package vn.edu.primary.teacher_support.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import vn.edu.primary.teacher_support.entity.ActionLog;
import vn.edu.primary.teacher_support.entity.Role;
import vn.edu.primary.teacher_support.entity.User;
import vn.edu.primary.teacher_support.repository.UserRepository;
import vn.edu.primary.teacher_support.service.ActionLogService;
import vn.edu.primary.teacher_support.service.JwtService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/action-logs")
@RequiredArgsConstructor
public class ActionLogController {
    private final ActionLogService actionLogService;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @GetMapping({"", "/"})
    public Page<ActionLog> search(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(required = false) String identity,
            @RequestParam(required = false) String resourceId,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        authenticateAdmin(authorization);
        int safeSize = Math.min(Math.max(size, 1), 500);
        return actionLogService.search(identity, module, action, resourceId, severity, status, from, to,
                PageRequest.of(Math.max(page, 0), safeSize, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @GetMapping("/{id}")
    public ActionLog detail(@RequestHeader("Authorization") String authorization, @PathVariable Long id) {
        authenticateAdmin(authorization);
        return actionLogService.getById(id);
    }

    private void authenticateAdmin(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) throw new RuntimeException("Thiếu token xác thực");
        String raw = authorization.substring(7).trim();
        if (raw.toLowerCase().startsWith("bearer ")) raw = raw.substring(7).trim();
        final String token = raw;
        if (!jwtService.isValid(token)) throw new RuntimeException("Token không hợp lệ hoặc đã hết hạn");
        User user = resolveUser(token);
        if (user.getRole() != Role.RoleName.ADMIN && user.getRoles().stream().noneMatch(role -> role.getName() == Role.RoleName.ADMIN)) {
            throw new RuntimeException("Bạn không có quyền xem nhật ký hành động");
        }
    }

    private User resolveUser(final String token) {
        String username = jwtService.extractUsername(token);
        return userRepository.findByUsername(username)
                .or(() -> {
                    Long userId = jwtService.extractUserId(token);
                    return userId == null ? java.util.Optional.empty() : userRepository.findById(userId);
                })
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
    }
}
