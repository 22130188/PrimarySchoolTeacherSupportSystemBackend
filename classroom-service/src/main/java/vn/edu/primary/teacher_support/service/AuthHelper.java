package vn.edu.primary.teacher_support.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.edu.primary.teacher_support.dto.UserDto;
import vn.edu.primary.teacher_support.exception.ForbiddenException;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthHelper {

    private final JwtService jwtService;
    private final UserServiceClient userServiceClient;

    public Long extractUserId(String authorization) {
        String token = resolveToken(authorization);
        String email = jwtService.extractEmail(token);
        UserDto user = userServiceClient.findByEmail(email)
                .orElseThrow(() -> new ForbiddenException("Không tìm thấy người dùng với email: " + email));
        return user.getId();
    }

    public String extractEmail(String authorization) {
        String token = resolveToken(authorization);
        return jwtService.extractEmail(token);
    }

    public String extractRole(String authorization) {
        String token = resolveToken(authorization);
        return jwtService.extractPrimaryRole(token);
    }

    public void validateAdmin(String authorization) {
        boolean hasBearer = authorization != null && authorization.startsWith("Bearer ");
        log.info("validateAdmin hasBearer={} authLen={}", hasBearer,
                authorization == null ? 0 : authorization.length());
        String role;
        try {
            role = extractRole(authorization);
        } catch (Exception e) {
            log.warn("validateAdmin JWT parse failed: {}", e.getMessage());
            throw new ForbiddenException("Token không hợp lệ hoặc JWT_SECRET không khớp: " + e.getMessage());
        }
        log.info("validateAdmin role={}", role);
        if (role == null || !"ADMIN".equalsIgnoreCase(role.trim())) {
            throw new ForbiddenException("Chỉ admin mới có quyền thực hiện (role=" + role + ")");
        }
    }

    public void validateTeacherOrAdmin(String authorization) {
        String role = extractRole(authorization);
        if (!"TEACHER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            throw new ForbiddenException("Chỉ giáo viên hoặc admin mới có quyền thực hiện");
        }
    }
    public void validateTeacher(String authorization) {
        String role = extractRole(authorization);
        if (!"TEACHER".equalsIgnoreCase(role)) {
            throw new ForbiddenException("Chỉ giáo viên mới có quyền thực hiện");
        }
    }


    public void validateStudent(String authorization) {
        String role = extractRole(authorization);
        if (!"STUDENT".equalsIgnoreCase(role)) {
            throw new ForbiddenException("Chỉ học sinh mới có quyền thực hiện");
        }
    }

    private String resolveToken(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        throw new ForbiddenException("Token không hợp lệ");
    }
}
