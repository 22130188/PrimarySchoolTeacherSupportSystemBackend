package vn.edu.primary.teacher_support.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationClient {

    private static final String NOTIFICATION_URL =
            "http://user-service:8082/api/internal/users/notifications";

    private final RestTemplate restTemplate;

    public void notifyUser(Long targetUserId, Long actorUserId, String actorName, String type,
                           String title, String message, String actionUrl,
                           String resourceType, Object resourceId) {
        if (targetUserId == null) return;
        notifyUsers(List.of(targetUserId), actorUserId, actorName, type, title, message,
                actionUrl, resourceType, resourceId);
    }

    public void notifyUsers(Collection<Long> targetUserIds, Long actorUserId, String actorName, String type,
                            String title, String message, String actionUrl,
                            String resourceType, Object resourceId) {
        if (targetUserIds == null || targetUserIds.isEmpty()) return;
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("targetUserIds", targetUserIds.stream().filter(java.util.Objects::nonNull).distinct().toList());
            payload.put("actorUserId", actorUserId);
            payload.put("actorName", actorName);
            payload.put("type", type);
            payload.put("title", title);
            payload.put("message", message);
            payload.put("actionUrl", actionUrl);
            payload.put("resourceType", resourceType);
            payload.put("resourceId", resourceId == null ? null : String.valueOf(resourceId));
            restTemplate.postForEntity(NOTIFICATION_URL, payload, Map.class);
        } catch (Exception e) {
            log.warn("Could not create notification type={} for users={}: {}",
                    type, targetUserIds, e.getMessage());
        }
    }
}
