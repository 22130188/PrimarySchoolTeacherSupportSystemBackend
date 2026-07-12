package vn.edu.primary.teacher_support.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.primary.teacher_support.dto.NotificationCreateRequest;
import vn.edu.primary.teacher_support.entity.Feedback;
import vn.edu.primary.teacher_support.entity.User;
import vn.edu.primary.teacher_support.repository.FeedbackRepository;
import vn.edu.primary.teacher_support.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service @RequiredArgsConstructor
public class FeedbackService {
    private static final Set<String> TYPES = Set.of("BUG", "SUGGESTION");
    private static final Set<String> STATUSES = Set.of("NEW", "IN_PROGRESS", "RESOLVED", "CLOSED");
    private final FeedbackRepository repository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Transactional
    public Feedback create(User user, CreateRequest request) {
        String type = normalize(request.type(), TYPES, "Loại phản hồi không hợp lệ");
        Feedback item = new Feedback(); item.setUserId(user.getId());
        item.setUserName(user.getFullName() == null || user.getFullName().isBlank() ? user.getUsername() : user.getFullName());
        item.setUserEmail(user.getEmail()); item.setType(type); item.setTitle(request.title().trim());
        item.setDescription(request.description().trim()); item.setPageUrl(trimToNull(request.pageUrl()));
        item.setBrowserInfo(trimToNull(request.browserInfo())); item.setStatus("NEW");
        Feedback saved = repository.save(item);
        notifyAdmins(user, saved);
        return saved;
    }

    private void notifyAdmins(User sender, Feedback feedback) {
        NotificationCreateRequest request = new NotificationCreateRequest();
        request.setTargetUserIds(userRepository.findByRoleAndIsActiveTrue(vn.edu.primary.teacher_support.entity.Role.RoleName.ADMIN)
                .stream().map(User::getId).toList());
        request.setActorUserId(sender.getId());
        request.setActorName(feedback.getUserName());
        request.setType("NEW_FEEDBACK");
        request.setTitle(("BUG".equals(feedback.getType()) ? "Báo lỗi mới: " : "Góp ý mới: ") + feedback.getTitle());
        request.setMessage(feedback.getDescription());
        request.setActionUrl("/admin?section=feedback");
        request.setResourceType("FEEDBACK");
        request.setResourceId(String.valueOf(feedback.getId()));
        notificationService.create(request);
    }
    @Transactional(readOnly = true)
    public List<Feedback> search(String status, String type, String keyword) {
        String wantedStatus = status == null || status.equalsIgnoreCase("ALL") ? null : normalize(status, STATUSES, "Trạng thái không hợp lệ");
        String wantedType = type == null || type.equalsIgnoreCase("ALL") ? null : normalize(type, TYPES, "Loại phản hồi không hợp lệ");
        String term = keyword == null ? "" : keyword.trim().toLowerCase();
        return repository.findAllByOrderByCreatedAtDesc().stream()
                .filter(item -> wantedStatus == null || item.getStatus().equals(wantedStatus))
                .filter(item -> wantedType == null || item.getType().equals(wantedType))
                .filter(item -> term.isEmpty() || contains(item.getTitle(), term) || contains(item.getDescription(), term)
                        || contains(item.getUserName(), term) || contains(item.getUserEmail(), term))
                .toList();
    }

    @Transactional
    public Feedback updateStatus(Long id, String status) {
        Feedback item = get(id); item.setStatus(normalize(status, STATUSES, "Trạng thái không hợp lệ")); return repository.save(item);
    }

    @Transactional
    public Feedback reply(Long id, User admin, ReplyRequest request) {
        Feedback item = get(id); String message = request.message().trim();
        item.setAdminReply(message); item.setRepliedBy(admin.getId()); item.setRepliedAt(LocalDateTime.now());
        item.setStatus(request.status() == null || request.status().isBlank() ? "RESOLVED" : normalize(request.status(), STATUSES, "Trạng thái không hợp lệ"));
        repository.save(item);
        NotificationCreateRequest notification = new NotificationCreateRequest(); notification.setTargetUserId(item.getUserId());
        notification.setActorUserId(admin.getId()); notification.setActorName(admin.getFullName()); notification.setType("FEEDBACK_REPLY");
        notification.setTitle("Phản hồi từ quản trị viên: " + item.getTitle()); notification.setMessage(message);
        notification.setActionUrl("/dashboard"); notification.setResourceType("FEEDBACK"); notification.setResourceId(String.valueOf(item.getId()));
        notificationService.create(notification); return item;
    }

    private Feedback get(Long id) { return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phản hồi")); }
    private static boolean contains(String value, String term) { return value != null && value.toLowerCase().contains(term); }
    private static String trimToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private static String normalize(String value, Set<String> allowed, String message) { String normalized = value == null ? "" : value.trim().toUpperCase(); if (!allowed.contains(normalized)) throw new IllegalArgumentException(message); return normalized; }

    public record CreateRequest(String type, String title, String description, String pageUrl, String browserInfo) {}
    public record ReplyRequest(String message, String status) {}
}
