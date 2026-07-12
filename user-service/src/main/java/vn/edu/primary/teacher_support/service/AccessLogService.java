package vn.edu.primary.teacher_support.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.primary.teacher_support.entity.AccessLog;
import vn.edu.primary.teacher_support.entity.User;
import vn.edu.primary.teacher_support.repository.AccessLogRepository;

@Service
@RequiredArgsConstructor
public class AccessLogService {
    private final AccessLogRepository accessLogRepository;

    public void recordLogin(String username, User user, boolean success, HttpServletRequest request) {
        AccessLog log = new AccessLog();
        log.setUsername(username == null || username.isBlank() ? "Không xác định" : username.trim());
        log.setUserId(user == null ? null : user.getId());
        log.setRole(user == null || user.getRole() == null ? null : user.getRole().name());
        log.setAction(success ? "LOGIN" : "LOGIN_FAILED");
        log.setStatus(success ? "success" : "failed");
        log.setIp(clientIp(request));
        log.setUserAgent(request.getHeader("User-Agent"));
        accessLogRepository.save(log);
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        return realIp == null || realIp.isBlank() ? request.getRemoteAddr() : realIp.trim();
    }
}
