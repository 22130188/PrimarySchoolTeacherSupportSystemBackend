package vn.edu.primary.teacher_support.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActionLogClient {
    private static final String ACTION_LOG_URL = "http://user-service/api/internal/action-logs";

    private final RestTemplate restTemplate;

    public void log(String username, String action, String module, String resourceId,
                    String httpMethod, String endpoint, String severity, String status, String description) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("username", username);
            payload.put("action", action);
            payload.put("module", module);
            payload.put("resourceId", resourceId);
            payload.put("httpMethod", httpMethod);
            payload.put("endpoint", endpoint);
            payload.put("severity", severity == null ? "WARNING" : severity);
            payload.put("status", status == null ? "SUCCESS" : status);
            payload.put("description", description);
            restTemplate.postForEntity(ACTION_LOG_URL, payload, Map.class);
        } catch (Exception e) {
            log.warn("Không ghi được action log action={}: {}", action, e.getMessage());
        }
    }

    public void logAuthenticated(String authorization, String action, String resourceId,
            String httpMethod, String endpoint, String severity, String description, String ipAddress) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("authToken", authorization);
            payload.put("action", action);
            payload.put("module", "classrooms");
            payload.put("resourceId", resourceId);
            payload.put("httpMethod", httpMethod);
            payload.put("endpoint", endpoint);
            payload.put("severity", severity == null ? "WARNING" : severity);
            payload.put("status", "SUCCESS");
            payload.put("description", description);
            payload.put("ipAddress", ipAddress);
            restTemplate.postForEntity(ACTION_LOG_URL, payload, Map.class);
        } catch (Exception exception) {
            log.warn("Không ghi được action log action={}: {}", action, exception.getMessage());
        }
    }

}
