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
        // Prefer claim userId (no network hop)
        Long claimUserId = jwtService.extractUserId(token);
        if (claimUserId != null && claimUserId > 0) {
            return claimUserId;
        }
        String email = jwtService.extractEmail(token);
        if (email != null && !email.isBlank()) {
            UserDto user = userServiceClient.findByEmail(email)
                    .orElseThrow(() -> new ForbiddenException("Không tìm thấy người dùng với email: " + email));
            return user.getId();
        }
        throw new ForbiddenException("Token không chứa userId/email");
    }

    public String extractRole(String authorization) {
        String token = resolveToken(authorization);
        return jwtService.extractPrimaryRole(token);
    }

    public void validateTeacherOrAdmin(String authorization) {
        String role = extractRole(authorization);
        if (!"TEACHER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            throw new ForbiddenException("Chỉ giáo viên hoặc admin mới có quyền thực hiện");
        }
    }

    public void validateAdmin(String authorization) {
        String role = extractRole(authorization);
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new ForbiddenException("Chi admin moi co quyen thuc hien");
        }
    }

    private String resolveToken(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7).trim();
        }
        throw new ForbiddenException("Token không hợp lệ");
    }
}
