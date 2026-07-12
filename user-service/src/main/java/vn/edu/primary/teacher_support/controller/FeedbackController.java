package vn.edu.primary.teacher_support.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.primary.teacher_support.entity.Feedback;
import vn.edu.primary.teacher_support.entity.Role;
import vn.edu.primary.teacher_support.entity.User;
import vn.edu.primary.teacher_support.repository.UserRepository;
import vn.edu.primary.teacher_support.service.FeedbackService;
import vn.edu.primary.teacher_support.service.JwtService;
import java.util.List;

@RestController @RequiredArgsConstructor
public class FeedbackController {
    private final FeedbackService service; private final JwtService jwtService; private final UserRepository userRepository;

    @PostMapping("/api/user/feedback")
    public Feedback create(@RequestHeader(value="Authorization", required=false) String auth, @Valid @RequestBody CreateBody body) {
        User user = authenticate(auth); return service.create(user, new FeedbackService.CreateRequest(body.type(), body.title(), body.description(), body.pageUrl(), body.browserInfo()));
    }
    @GetMapping("/api/admin/feedback")
    public List<Feedback> all(@RequestHeader(value="Authorization", required=false) String auth, @RequestParam(required=false) String status, @RequestParam(required=false) String type, @RequestParam(required=false) String keyword) { admin(auth); return service.search(status, type, keyword); }
    @PatchMapping("/api/admin/feedback/{id}/status")
    public Feedback status(@RequestHeader(value="Authorization", required=false) String auth, @PathVariable Long id, @Valid @RequestBody StatusBody body) { admin(auth); return service.updateStatus(id, body.status()); }
    @PostMapping("/api/admin/feedback/{id}/reply")
    public Feedback reply(@RequestHeader(value="Authorization", required=false) String auth, @PathVariable Long id, @Valid @RequestBody ReplyBody body) { User admin=admin(auth); return service.reply(id, admin, new FeedbackService.ReplyRequest(body.message(), body.status())); }

    private User authenticate(String authorization) { if (authorization==null || !authorization.startsWith("Bearer ")) throw new RuntimeException("Thiếu token xác thực"); String token=authorization.substring(7); if (!jwtService.isValid(token)) throw new RuntimeException("Token không hợp lệ hoặc đã hết hạn"); return userRepository.findByUsername(jwtService.extractUsername(token)).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng")); }
    private User admin(String authorization) { User user=authenticate(authorization); if (user.getRole()!=Role.RoleName.ADMIN) throw new RuntimeException("Bạn không có quyền quản lý phản hồi"); return user; }

    public record CreateBody(@NotBlank String type, @NotBlank @Size(max=180) String title, @NotBlank @Size(min=10,max=3000) String description, @Size(max=1000) String pageUrl, @Size(max=1000) String browserInfo) {}
    public record StatusBody(@NotBlank String status) {}
    public record ReplyBody(@NotBlank @Size(max=3000) String message, String status) {}
}
