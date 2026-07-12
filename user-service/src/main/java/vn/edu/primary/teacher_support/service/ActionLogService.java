package vn.edu.primary.teacher_support.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.primary.teacher_support.dto.ActionLogCreateRequest;
import vn.edu.primary.teacher_support.entity.ActionLog;
import vn.edu.primary.teacher_support.entity.User;
import vn.edu.primary.teacher_support.repository.ActionLogRepository;
import vn.edu.primary.teacher_support.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActionLogService {
    private final ActionLogRepository actionLogRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Async
    @Transactional
    public void createAsync(ActionLogCreateRequest request) {
        create(request);
    }

    @Transactional
    public ActionLog create(ActionLogCreateRequest request) {
        User user = resolveUser(request);
        ActionLog log = new ActionLog();
        log.setUserId(user == null ? null : user.getId());
        log.setUsername(user == null ? null : user.getUsername());
        log.setClientIdentifier(user == null ? normalizeClient(request) : null);
        log.setAction(required(request.getAction(), "UNKNOWN_ACTION", 100));
        log.setModule(required(request.getModule(), "system", 80));
        log.setResourceId(limit(request.getResourceId(), 150));
        log.setHttpMethod(required(request.getHttpMethod(), "UNKNOWN", 10).toUpperCase());
        log.setEndpoint(required(request.getEndpoint(), "/unknown", 500));
        log.setSeverity(parseSeverity(request.getSeverity()));
        log.setStatus("FAILED".equalsIgnoreCase(request.getStatus()) ? "FAILED" : "SUCCESS");
        log.setDescription(limit(request.getDescription(), 10000));
        log.setIpAddress(limit(request.getIpAddress(), 64));
        return actionLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public Page<ActionLog> search(String identity, String module, String action, String severity,
                                  String status, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        Specification<ActionLog> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (identity != null && !identity.isBlank()) {
                String keyword = "%" + identity.trim().toLowerCase() + "%";
                predicates.add(cb.or(cb.like(cb.lower(root.get("username")), keyword),
                        cb.like(cb.lower(root.get("clientIdentifier")), keyword)));
            }
            if (module != null && !module.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("module")), "%" + module.trim().toLowerCase() + "%"));
            }
            if (action != null && !action.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("action")), "%" + action.trim().toLowerCase() + "%"));
            }
            if (severity != null && !severity.isBlank()) predicates.add(cb.equal(root.get("severity"), parseSeverity(severity)));
            if (status != null && !status.isBlank()) predicates.add(cb.equal(root.get("status"), status.toUpperCase()));
            if (from != null) predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            if (to != null) predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        return actionLogRepository.findAll(specification, pageable);
    }

    @Transactional(readOnly = true)
    public ActionLog getById(Long id) {
        return actionLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy action log: " + id));
    }

    private User resolveUser(ActionLogCreateRequest request) {
        String token = extractToken(request.getAuthToken());
        if (token != null && jwtService.isValid(token)) {
            return userRepository.findByUsername(jwtService.extractUsername(token)).orElse(null);
        }
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            return userRepository.findByUsername(request.getUsername().trim()).orElse(null);
        }
        return null;
    }

    private String extractToken(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String value = raw.trim();
        if (value.regionMatches(true, 0, "Bearer ", 0, 7)) {
            value = value.substring(7).trim();
        }
        // Đôi khi token bị double "Bearer Bearer ..."
        if (value.regionMatches(true, 0, "Bearer ", 0, 7)) {
            value = value.substring(7).trim();
        }
        return value.isBlank() ? null : value;
    }

    private String normalizeClient(ActionLogCreateRequest request) {
        if (request.getClientIdentifier() != null && !request.getClientIdentifier().isBlank()) {
            return limit(request.getClientIdentifier().trim(), 255);
        }
        String ip = request.getIpAddress();
        return ip == null || ip.isBlank() ? "unknown_client" : "guest_" + limit(ip, 240);
    }

    private ActionLog.Severity parseSeverity(String value) {
        try { return ActionLog.Severity.valueOf(value == null ? "INFO" : value.toUpperCase()); }
        catch (IllegalArgumentException ignored) { return ActionLog.Severity.INFO; }
    }

    private String required(String value, String fallback, int max) {
        return value == null || value.isBlank() ? fallback : limit(value.trim(), max);
    }

    private String limit(String value, int max) {
        return value == null || value.length() <= max ? value : value.substring(0, max);
    }
}
