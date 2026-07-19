package vn.edu.primary.teacher_support.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.edu.primary.teacher_support.entity.*;
import vn.edu.primary.teacher_support.repository.*;
import vn.edu.primary.teacher_support.service.JwtService;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {
    private final UserRepository userRepository;
    private final FeedbackRepository feedbackRepository;
    private final JwtService jwtService;
    private final ActionLogRepository actionLogRepository;

    @GetMapping("/overview")
    public OverviewResponse overview(@RequestHeader(value = "Authorization", required = false) String authorization) {
        authenticateAdmin(authorization);
        int year = LocalDateTime.now().getYear();
        int[] usersByMonth = new int[12];
        int[] feedbackByMonth = new int[12];
        List<User> users = userRepository.findAll();
        List<Feedback> feedback = feedbackRepository.findAllByOrderByCreatedAtDesc();
        users.stream().filter(user -> user.getCreatedAt() != null && user.getCreatedAt().getYear() == year)
                .forEach(user -> usersByMonth[user.getCreatedAt().getMonthValue() - 1]++);
        feedback.stream().filter(item -> item.getCreatedAt() != null && item.getCreatedAt().getYear() == year)
                .forEach(item -> feedbackByMonth[item.getCreatedAt().getMonthValue() - 1]++);
        List<MonthlyActivity> monthly = new ArrayList<>();
        for (int month = 1; month <= 12; month++) monthly.add(new MonthlyActivity(month, usersByMonth[month - 1], feedbackByMonth[month - 1]));

        // Calculate AI Usage from Action Logs
        // Module names in DB are lowercase: 'tts', 'images', 'pronunciation'
        // Translate does not have its own module — it uses module='lessons' with action='CREATE_BILINGUAL_LESSON'
        List<Object[]> aiCounts = actionLogRepository.countByModules(List.of("tts", "images", "pronunciation"));
        Map<String, Long> aiUsage = new LinkedHashMap<>();
        aiUsage.put("TTS", 0L);
        aiUsage.put("IMAGE", 0L);
        aiUsage.put("PRONUNCIATION", 0L);
        aiUsage.put("TRANSLATE", 0L);
        for (Object[] row : aiCounts) {
            if (row.length >= 2 && row[0] != null) {
                String mod = row[0].toString().toLowerCase();
                long count = ((Number) row[1]).longValue();
                if ("tts".equals(mod)) aiUsage.put("TTS", count);
                else if ("images".equals(mod)) aiUsage.put("IMAGE", count);
                else if ("pronunciation".equals(mod)) aiUsage.put("PRONUNCIATION", count);
            }
        }
        // Count translate (bilingual lesson) actions separately
        long translateCount = actionLogRepository.countByAction("CREATE_BILINGUAL_LESSON");
        aiUsage.put("TRANSLATE", translateCount);

        // Calculate Feedback Stats by status
        Map<String, Long> feedbackStats = new HashMap<>();
        feedbackStats.put("NEW", 0L);
        feedbackStats.put("IN_PROGRESS", 0L);
        feedbackStats.put("RESOLVED", 0L);
        feedbackStats.put("CLOSED", 0L);
        for (Feedback item : feedback) {
            String status = item.getStatus() != null ? item.getStatus().toUpperCase() : "NEW";
            feedbackStats.put(status, feedbackStats.getOrDefault(status, 0L) + 1);
        }

        return new OverviewResponse(year, monthly, recentActivities(users, feedback), aiUsage, feedbackStats);
    }

    private List<RecentActivity> recentActivities(List<User> users, List<Feedback> feedback) {
        List<RecentActivity> result = new ArrayList<>();
        users.forEach(user -> result.add(new RecentActivity("USER_REGISTERED", user.getFullName(), "đã đăng ký tài khoản",
                user.getRole().name(), user.getCreatedAt(), String.valueOf(user.getId()))));
        feedback.forEach(item -> result.add(new RecentActivity("NEW_FEEDBACK", item.getUserName(),
                "BUG".equals(item.getType()) ? "đã gửi báo lỗi" : "đã gửi góp ý", item.getTitle(), item.getCreatedAt(), String.valueOf(item.getId()))));
        return result.stream().filter(item -> item.createdAt() != null)
                .sorted(Comparator.comparing(RecentActivity::createdAt).reversed()).limit(8).toList();
    }

    private void authenticateAdmin(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) throw new RuntimeException("Thiếu token xác thực");
        String token = authorization.substring(7);
        if (!jwtService.isValid(token)) throw new RuntimeException("Token không hợp lệ hoặc đã hết hạn");
        User user = userRepository.findByUsername(jwtService.extractUsername(token))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        if (user.getRole() != Role.RoleName.ADMIN && user.getRoles().stream().noneMatch(r -> r.getName() == Role.RoleName.ADMIN)) throw new RuntimeException("Bạn không có quyền xem tổng quan");
    }

    public record OverviewResponse(
            int year, 
            List<MonthlyActivity> monthlyActivity, 
            List<RecentActivity> recentActivities,
            Map<String, Long> aiUsage,
            Map<String, Long> feedbackStats
    ) {}
    public record MonthlyActivity(int month, int newUsers, int feedbackCount) {}
    public record RecentActivity(String type, String actor, String action, String subject, LocalDateTime createdAt, String resourceId) {}
}