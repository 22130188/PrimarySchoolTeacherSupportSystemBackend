package vn.edu.primary.teacher_support.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.primary.teacher_support.dto.UpdateProfileRequest;
import vn.edu.primary.teacher_support.entity.User;
import vn.edu.primary.teacher_support.service.JwtService;
import vn.edu.primary.teacher_support.service.UserService;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final JwtService  jwtService;

    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService  = jwtService;
    }

    private String getUsernameFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            throw new RuntimeException("Token không hợp lệ");
        return jwtService.extractUsername(authHeader.substring(7));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(@RequestHeader("Authorization") String authHeader) {
        String username = getUsernameFromHeader(authHeader);
        return ResponseEntity.ok(userService.toDTO(userService.findByUsername(username)));
    }

    @PutMapping("/personal")
    public ResponseEntity<?> updatePersonal(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody UpdateProfileRequest req) {
        return ResponseEntity.ok(userService.toDTO(
                userService.updatePersonal(getUsernameFromHeader(authHeader), req)));
    }

    @PutMapping("/school")
    public ResponseEntity<?> updateSchool(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody UpdateProfileRequest req) {
        return ResponseEntity.ok(userService.toDTO(
                userService.updateSchool(getUsernameFromHeader(authHeader), req)));
    }

    @PutMapping("/classes")
    public ResponseEntity<?> updateClasses(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody UpdateProfileRequest req) {
        return ResponseEntity.ok(userService.toDTO(
                userService.updateClasses(getUsernameFromHeader(authHeader), req.getClasses())));
    }

    // Nhận URL Cloudinary từ frontend
    @PutMapping("/avatar-url")
    public ResponseEntity<?> updateAvatarUrl(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> body) {
        String avatarUrl = body.get("avatarUrl");
        if (avatarUrl == null || avatarUrl.isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "URL ảnh không hợp lệ"));
        return ResponseEntity.ok(userService.toDTO(
                userService.updateAvatarUrl(getUsernameFromHeader(authHeader), avatarUrl)));
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody UpdateProfileRequest req) {
        userService.changePassword(
                getUsernameFromHeader(authHeader),
                req.getCurrentPassword(), req.getNewPassword());
        return ResponseEntity.ok("Đổi mật khẩu thành công");
    }
}