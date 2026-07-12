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
        return new OverviewResponse(year, monthly, recentActivities(users, feedback));
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
        if (user.getRole() != Role.RoleName.ADMIN) throw new RuntimeException("Bạn không có quyền xem tổng quan");
    }

    public record OverviewResponse(int year, List<MonthlyActivity> monthlyActivity, List<RecentActivity> recentActivities) {}
    public record MonthlyActivity(int month, int newUsers, int feedbackCount) {}
    public record RecentActivity(String type, String actor, String action, String subject, LocalDateTime createdAt, String resourceId) {}
}