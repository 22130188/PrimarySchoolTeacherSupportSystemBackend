package vn.edu.primary.teacher_support.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassroomServiceClient {

    private final RestTemplate restTemplate;
    private static final String CLASSROOM_SERVICE_URL = "http://classroom-service:8085";

    public Optional<Map<String, Object>> findById(Long classroomId) {
        try {
            @SuppressWarnings("unchecked")
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    CLASSROOM_SERVICE_URL + "/api/internal/classrooms/" + classroomId,
                    Map.class);
            if (response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> body = response.getBody();
                return Optional.of(body);
            }
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Failed to fetch classroom by id {}: {}", classroomId, e.getMessage());
            return Optional.empty();
        }
    }

    public String getClassroomName(Long classroomId) {
        return findById(classroomId)
                .map(m -> (String) m.get("name"))
                .orElse("Unknown");
    }

    public Long getClassroomTeacherId(Long classroomId) {
        return findById(classroomId)
                .map(m -> {
                    Object tid = m.get("teacherId");
                    if (tid instanceof Number) return ((Number) tid).longValue();
                    return null;
                })
                .orElse(null);
    }
    
    public boolean isWritable(Long classroomId) {
        return findById(classroomId)
                .map(classroom -> Boolean.TRUE.equals(classroom.get("writable")))
                .orElse(false);
    }

    public boolean hasAccess(Long classroomId, Long userId) {
        try {
            @SuppressWarnings("unchecked")
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    CLASSROOM_SERVICE_URL + "/api/internal/classrooms/" + classroomId + "/check-access/" + userId,
                    Map.class);
            if (response.getBody() != null) {
                Object hasAccess = response.getBody().get("hasAccess");
                if (hasAccess instanceof Boolean) return (Boolean) hasAccess;
            }
            return false;
        } catch (Exception e) {
            log.warn("Failed to check access for classroom {} user {}: {}", classroomId, userId, e.getMessage());
            return false;
        }
    }

    public List<Long> getStudentIds(Long classroomId) {
        try {
            @SuppressWarnings("unchecked")
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    CLASSROOM_SERVICE_URL + "/api/internal/classrooms/" + classroomId + "/notification-recipients",
                    Map.class);
            Object value = response.getBody() == null ? null : response.getBody().get("studentIds");
            if (value instanceof List<?> list) {
                return list.stream().filter(Number.class::isInstance)
                        .map(Number.class::cast).map(Number::longValue).toList();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch notification recipients for classroom {}: {}", classroomId, e.getMessage());
        }
        return List.of();
    }
}
