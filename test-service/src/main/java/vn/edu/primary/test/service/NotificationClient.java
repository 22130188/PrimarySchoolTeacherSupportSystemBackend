package vn.edu.primary.test.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationClient {
    private final RestTemplate restTemplate;

    @Value("${user.service.url:http://localhost:8082/api}")
    private String userServiceUrl;

    public void notifyTestSubmitted(Long teacherId, Long studentId, String studentName,
                                    Long testId, String testName, Integer score, Integer maxScore) {
        if (teacherId == null) return;
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("targetUserId", teacherId);
            body.put("actorUserId", studentId);
            body.put("actorName", studentName);
            body.put("type", "TEST_SUBMITTED");
            body.put("title", studentName + " đã nộp bài " + testName);
            body.put("message", "Điểm: " + score + "/" + maxScore);
            body.put("actionUrl", "/tests/" + testId + "/edit");
            body.put("resourceType", "TEST");
            body.put("resourceId", String.valueOf(testId));
            restTemplate.postForEntity(userServiceUrl + "/internal/users/notifications", body, Map.class);
        } catch (Exception e) {
            log.warn("Could not create test submission notification: {}", e.getMessage());
        }
    }
}
