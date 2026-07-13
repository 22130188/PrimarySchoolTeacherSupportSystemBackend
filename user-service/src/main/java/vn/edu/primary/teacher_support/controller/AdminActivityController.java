package vn.edu.primary.teacher_support.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.primary.teacher_support.entity.ActionLog;
import vn.edu.primary.teacher_support.entity.Role;
import vn.edu.primary.teacher_support.entity.User;
import vn.edu.primary.teacher_support.repository.ActionLogRepository;
import vn.edu.primary.teacher_support.repository.UserRepository;
import vn.edu.primary.teacher_support.service.JwtService;
import vn.edu.primary.teacher_support.util.ActionLogLabels;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminActivityController {
    private static final Set<String> NOISE_ACTIONS = Set.of(
            "VIEW_ACTION_LOGS_LIST",
            "VIEW_ACTION_LOGS_DETAIL",
            "VIEW_DASHBOARD_LIST",
            "VIEW_DASHBOARD_DETAIL",
            "VIEW_USER_LIST",
            "VIEW_USER_DETAIL",
            "VIEW_NOTIFICATIONS_LIST",
            "VIEW_NOTIFICATIONS_DETAIL"
    );

    private final ActionLogRepository actionLogRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @GetMapping("/activity")
    public ActivityResponse activity(@RequestHeader("Authorization") String authorization) {
        authenticateAdmin(authorization);
        int year = LocalDateTime.now().getYear();
        List<User> users = userRepository.findAll();
        List<MonthlyActivity> monthly = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            final int currentMonth = month;
            YearMonth yearMonth = YearMonth.of(year, month);
            LocalDateTime from = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime to = yearMonth.plusMonths(1).atDay(1).atStartOfDay();
            long sessions = actionLogRepository.countByStatusAndCreatedAtBetween("SUCCESS", from, to);
            long newUsers = users.stream().filter(user -> user.getCreatedAt() != null
                    && user.getCreatedAt().getYear() == year && user.getCreatedAt().getMonthValue() == currentMonth).count();
            monthly.add(new MonthlyActivity(month, sessions, newUsers));
        }
        List<RecentActivity> recent = actionLogRepository.findTop30ByOrderByCreatedAtDesc().stream()
                .filter(log -> log.getAction() == null || !NOISE_ACTIONS.contains(log.getAction()))
                .filter(log -> !isNoiseEndpoint(log.getEndpoint()))
                .limit(8)
                .map(log -> new RecentActivity(
                        log.getAction(),
                        actor(log),
                        ActionLogLabels.describe(log.getAction(), log.getStatus(), log.getDescription()),
                        extractClassroomName(log.getDescription()),
                        log.getCreatedAt(),
                        String.valueOf(log.getId())
                )).toList();
        return new ActivityResponse(year, monthly, recent);
    }

    private String extractClassroomName(String description) {
        if (description == null || description.isBlank()) return null;
        int key = description.indexOf("\"classroomName\"");
        if (key < 0) return null;
        int colon = description.indexOf(':', key);
        int firstQuote = description.indexOf('"', colon + 1);
        int secondQuote = firstQuote < 0 ? -1 : description.indexOf('"', firstQuote + 1);
        if (firstQuote < 0 || secondQuote < 0) return null;
        String name = description.substring(firstQuote + 1, secondQuote).trim();
        return name.isEmpty() ? null : name;
    }

    private boolean isNoiseEndpoint(String endpoint) {
        if (endpoint == null) return false;
        String path = endpoint.toLowerCase();
        return path.contains("/notifications/unread")
                || path.contains("/action-logs")
                || path.contains("/admin/dashboard")
                || path.endsWith("/api/user/me")
                || path.contains("/user/me");
    }

    private String actor(ActionLog log) {
        return log.getUsername() != null ? log.getUsername() : log.getClientIdentifier();
    }

    private void authenticateAdmin(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) throw new RuntimeException("Thiếu token xác thực");
        String raw = authorization.substring(7).trim();
        if (raw.toLowerCase().startsWith("bearer ")) raw = raw.substring(7).trim();
        final String token = raw;
        if (!jwtService.isValid(token)) throw new RuntimeException("Token không hợp lệ hoặc đã hết hạn");
        User user = userRepository.findByUsername(jwtService.extractUsername(token))
                .or(() -> {
                    Long userId = jwtService.extractUserId(token);
                    return userId == null ? java.util.Optional.empty() : userRepository.findById(userId);
                })
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        if (user.getRole() != Role.RoleName.ADMIN && user.getRoles().stream().noneMatch(role -> role.getName() == Role.RoleName.ADMIN)) {
            throw new RuntimeException("Bạn không có quyền xem hoạt động hệ thống");
        }
    }

    public record ActivityResponse(int year, List<MonthlyActivity> monthlyActivity, List<RecentActivity> recentActivities) {}
    public record MonthlyActivity(int month, long sessions, long newUsers) {}
    /** subject = nhãn tiếng Việt của hành động (không còn endpoint thô). */
    public record RecentActivity(String type, String actor, String action, String subject, LocalDateTime createdAt, String resourceId) {}
}
