package vn.edu.primary.teacher_support.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.primary.teacher_support.dto.NotificationBroadcastRequest;
import vn.edu.primary.teacher_support.dto.NotificationCreateRequest;
import vn.edu.primary.teacher_support.dto.NotificationResponse;
import vn.edu.primary.teacher_support.entity.Notification;
import vn.edu.primary.teacher_support.entity.Role;
import vn.edu.primary.teacher_support.entity.User;
import vn.edu.primary.teacher_support.repository.NotificationRepository;
import vn.edu.primary.teacher_support.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(Long userId, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        return notificationRepository.findTop100ByTargetUserIdOrderByCreatedAtDesc(userId).stream()
                .limit(safeLimit)
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByTargetUserIdAndReadAtIsNull(userId);
    }

    @Transactional
    public NotificationResponse markRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findByIdAndTargetUserId(notificationId, userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo"));
        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
        return toResponse(notification);
    }

    @Transactional
    public int markAllRead(Long userId) {
        return notificationRepository.markAllRead(userId, LocalDateTime.now());
    }

    @Transactional
    public int create(NotificationCreateRequest request) {
        Set<Long> targetIds = new LinkedHashSet<>();
        if (request.getTargetUserId() != null) targetIds.add(request.getTargetUserId());
        if (request.getTargetUserIds() != null) targetIds.addAll(request.getTargetUserIds());
        targetIds.remove(null);
        if (targetIds.isEmpty()) return 0;

        String type = request.getType() == null || request.getType().isBlank()
                ? "SYSTEM" : request.getType().trim().toUpperCase();
        String title = request.getTitle() == null ? "Thông báo mới" : request.getTitle().trim();

        List<Notification> notifications = targetIds.stream()
                .map(targetId -> Notification.builder()
                        .targetUserId(targetId)
                        .actorUserId(request.getActorUserId())
                        .actorName(request.getActorName())
                        .type(type)
                        .title(title)
                        .message(request.getMessage())
                        .actionUrl(request.getActionUrl())
                        .resourceType(request.getResourceType())
                        .resourceId(request.getResourceId())
                        .build())
                .toList();
        notificationRepository.saveAll(notifications);
        return notifications.size();
    }

    @Transactional
    public int broadcast(NotificationBroadcastRequest request, User admin) {
        String targetRole = request.getTargetRole().trim().toUpperCase();
        List<User> recipients = switch (targetRole) {
            case "STUDENT" -> userRepository.findByRoleAndIsActiveTrue(Role.RoleName.STUDENT);
            case "TEACHER" -> userRepository.findByRoleAndIsActiveTrue(Role.RoleName.TEACHER);
            case "ALL" -> userRepository.findByIsActiveTrue().stream()
                    .filter(user -> user.getRole() != Role.RoleName.ADMIN)
                    .toList();
            default -> throw new IllegalArgumentException("Đối tượng nhận không hợp lệ");
        };

        NotificationCreateRequest createRequest = new NotificationCreateRequest();
        createRequest.setTargetUserIds(recipients.stream().map(User::getId).toList());
        createRequest.setActorUserId(admin.getId());
        createRequest.setActorName(admin.getFullName() != null ? admin.getFullName() : admin.getUsername());
        createRequest.setType("ADMIN_ANNOUNCEMENT");
        createRequest.setTitle(request.getTitle());
        createRequest.setMessage(request.getMessage());
        createRequest.setActionUrl(request.getActionUrl());
        createRequest.setResourceType("ADMIN");
        return create(createRequest);
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .actorUserId(notification.getActorUserId())
                .actorName(notification.getActorName())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .actionUrl(notification.getActionUrl())
                .resourceType(notification.getResourceType())
                .resourceId(notification.getResourceId())
                .read(notification.getReadAt() != null)
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
