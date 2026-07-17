package vn.edu.primary.teacher_support.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.primary.teacher_support.dto.*;
import vn.edu.primary.teacher_support.entity.Role;
import vn.edu.primary.teacher_support.entity.User;
import vn.edu.primary.teacher_support.repository.UserRepository;
import vn.edu.primary.teacher_support.service.JwtService;
import vn.edu.primary.teacher_support.service.UserManagementService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserManagementService userManagementService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role) {
        return ResponseEntity.ok(userManagementService.getUsers(keyword, role));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userManagementService.getUserById(id));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody CreateUserRequest request) {
        requireAdmin(authorization);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userManagementService.createUser(request, currentAdmin(authorization)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        requireAdmin(authorization);
        return ResponseEntity.ok(userManagementService.updateUser(id, request, currentAdmin(authorization)));
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<UserResponse> toggleUserStatus(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id) {
        requireAdmin(authorization);
        return ResponseEntity.ok(userManagementService.toggleUserStatus(id, currentAdmin(authorization)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id) {
        requireAdmin(authorization);
        userManagementService.deleteUser(id, currentAdmin(authorization));
        return ResponseEntity.noContent().build();
    }

    private User currentAdmin(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new RuntimeException("Thiếu token xác thực");
        }
        String token = authorization.substring(7).trim();
        if (!jwtService.isValid(token)) {
            throw new RuntimeException("Token không hợp lệ hoặc đã hết hạn");
        }
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new RuntimeException("Tài khoản đã bị khóa");
        }
        boolean isAdmin = user.getRole() == Role.RoleName.ADMIN
                || user.getRoles().stream().anyMatch(r -> r.getName() == Role.RoleName.ADMIN);
        if (!isAdmin) {
            throw new RuntimeException("Bạn không có quyền quản lý người dùng");
        }
        return user;
    }

    private void requireAdmin(String authorization) {
        currentAdmin(authorization);
    }
}
