package vn.edu.primary.teacher_support.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.primary.teacher_support.dto.*;
import vn.edu.primary.teacher_support.entity.User;
import vn.edu.primary.teacher_support.repository.UserRepository;
import vn.edu.primary.teacher_support.service.JwtService;
import vn.edu.primary.teacher_support.service.UserProfileService;

@RestController
@RequestMapping("/api/user")
public class UserProfileController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserProfileService userProfileService;

    public UserProfileController(JwtService jwtService,
                                UserRepository userRepository,
                                UserProfileService userProfileService) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.userProfileService = userProfileService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new RuntimeException("Thiếu token xác thực");
        }

        String token = authorization.substring(BEARER_PREFIX.length());
        if (!jwtService.isValid(token)) {
            throw new RuntimeException("Token không hợp lệ hoặc đã hết hạn");
        }

        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PutMapping("/personal")
    public ResponseEntity<UserResponse> updatePersonal(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody UpdatePersonalRequest request) {

        User user = authenticateAndGetUser(authorization);
        User updatedUser = userProfileService.updatePersonal(user, request);
        return ResponseEntity.ok(UserResponse.from(updatedUser));
    }

    @PutMapping("/school")
    public ResponseEntity<UserResponse> updateSchool(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody UpdateSchoolRequest request) {

        User user = authenticateAndGetUser(authorization);
        User updatedUser = userProfileService.updateSchool(user, request);
        return ResponseEntity.ok(UserResponse.from(updatedUser));
    }

    @PutMapping("/classes")
    public ResponseEntity<UserResponse> updateClasses(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody UpdateClassesRequest request) {

        User user = authenticateAndGetUser(authorization);
        User updatedUser = userProfileService.updateClasses(user, request);
        return ResponseEntity.ok(UserResponse.from(updatedUser));
    }

    @PutMapping("/avatar-url")
    public ResponseEntity<UserResponse> updateAvatar(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody UpdateAvatarRequest request) {

        User user = authenticateAndGetUser(authorization);
        User updatedUser = userProfileService.updateAvatar(user, request);
        return ResponseEntity.ok(UserResponse.from(updatedUser));
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody ChangePasswordRequest request) {

        User user = authenticateAndGetUser(authorization);
        userProfileService.changePassword(user, request);
        return ResponseEntity.ok("Đổi mật khẩu thành công");
    }

    private User authenticateAndGetUser(String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new RuntimeException("Thiếu token xác thực");
        }

        String token = authorization.substring(BEARER_PREFIX.length());
        if (!jwtService.isValid(token)) {
            throw new RuntimeException("Token không hợp lệ hoặc đã hết hạn");
        }

        String username = jwtService.extractUsername(token);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
    }
}
