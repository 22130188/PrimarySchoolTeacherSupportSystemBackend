package vn.edu.primary.test.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActionLogClient {
    private final RestTemplate restTemplate;

    @Value("${user.service.url:http://localhost:8082/api}")
    private String userServiceUrl;

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
            String base = userServiceUrl.endsWith("/api") ? userServiceUrl : userServiceUrl + "/api";
            restTemplate.postForEntity(base + "/internal/action-logs", payload, Map.class);
        } catch (Exception e) {
            log.warn("Không ghi được action log action={}: {}", action, e.getMessage());
        }
    }
}
