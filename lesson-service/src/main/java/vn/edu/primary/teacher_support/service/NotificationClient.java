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
    private final RestTemplate restTemplate;

    public void notifyUser(Long targetId, Long actorId, String actorName, String type, String title,
                           String message, String actionUrl, String resourceType, Object resourceId) {
        if (targetId == null) return;
        notifyUsers(List.of(targetId), actorId, actorName, type, title, message, actionUrl, resourceType, resourceId);
    }

    public void notifyUsers(Collection<Long> targets, Long actorId, String actorName, String type, String title,
                            String message, String actionUrl, String resourceType, Object resourceId) {
        if (targets == null || targets.isEmpty()) return;
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("targetUserIds", targets);
            body.put("actorUserId", actorId);
            body.put("actorName", actorName);
            body.put("type", type);
            body.put("title", title);
            body.put("message", message);
            body.put("actionUrl", actionUrl);
            body.put("resourceType", resourceType);
            body.put("resourceId", resourceId == null ? null : String.valueOf(resourceId));
            restTemplate.postForEntity("http://user-service/api/internal/users/notifications", body, Map.class);
        } catch (Exception e) {
            log.warn("Could not send lesson notification type={}: {}", type, e.getMessage());
        }
    }
}
