package mircoservices.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class ActionLoggingGlobalFilter implements GlobalFilter, Ordered {
    private static final Logger log = LoggerFactory.getLogger(ActionLoggingGlobalFilter.class);

    /** Endpoint chỉ phục vụ UI/poll — không ghi nhật ký hành động. */
    private static final List<String> SKIP_PATH_PREFIXES = List.of(
            "/health",
            "/actuator",
            "/login/oauth2/code/",
            "/api/internal/",
            "/api/action-logs",
            "/api/admin/dashboard",
            "/api/admin/access-logs",
            "/api/user/notifications",
            "/api/user/me",
            "/api/auth/refresh",
            "/api/auth/validate",
            // Dịch từng đoạn text khi tạo bản dịch bài giảng — không ghi log từng request
            "/api/translate"
    );

    private static final List<String> SKIP_EXACT_PATHS = List.of(
            "/api/auth/login"
    );

    private final WebClient webClient;

    public ActionLoggingGlobalFilter(@Value("${action-log.service-url:http://localhost:8082}") String serviceUrl) {
        this.webClient = WebClient.builder().baseUrl(serviceUrl).build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpMethod method = exchange.getRequest().getMethod();
        String path = exchange.getRequest().getURI().getPath();
        if (!shouldLog(method, path)) {
            return chain.filter(exchange);
        }

        ServerWebExchange forwarded = exchange.mutate().request(request ->
                request.header("X-Action-Logged-By-Gateway", "true")).build();
        long startedAt = System.currentTimeMillis();

        return chain.filter(forwarded)
                .doOnSuccess(ignored -> send(forwarded, method, path, startedAt, null))
                .doOnError(error -> send(forwarded, method, path, startedAt, error));
    }

    /**
     * Chỉ ghi log thao tác thay đổi / xuất dữ liệu — không ghi xem/browse.
     * GET /images/{userId}, /audios/{userId} là list theo user, không phải "xem chi tiết";
     * React StrictMode còn gọi đôi → 2 log trùng. Vì vậy GET thường không ghi.
     */
    static boolean shouldLog(HttpMethod method, String path) {
        if (method == null || path == null) return false;
        if (method == HttpMethod.OPTIONS) return false;

        String normalized = path.toLowerCase(Locale.ROOT);
        if (SKIP_EXACT_PATHS.stream().anyMatch(normalized::equals)) return false;
        if (SKIP_PATH_PREFIXES.stream().anyMatch(normalized::startsWith)) return false;

        if (method == HttpMethod.POST || method == HttpMethod.PUT
                || method == HttpMethod.PATCH || method == HttpMethod.DELETE) {
            // Các thao tác này được service ghi log chi tiết (kèm tên lớp).
            if (isClassroomPostMutation(normalized)) return false;
            if (isClassroomInviteMutation(normalized)) return false;
            if (isClassroomStatusMutation(normalized)) return false;
            if (isLessonClassroomShareMutation(normalized)) return false;
            if (isCanvasIntermediateUpload(normalized)) return false;
            // Tạo/sửa/xóa bài kiểm tra & bài tập: test-service ghi theo testType
            if (isTestRootMutation(method, normalized)) return false;
            return true;
        }

        if (method == HttpMethod.GET) {
            if (hasPathSegment(normalized, "logout")) return true;
            // Chỉ log GET khi xuất/tải file — không log xem danh sách hay "chi tiết" list-by-id.
            return hasPathSegment(normalized, "export") || hasPathSegment(normalized, "download");
        }

        return false;
    }

    /** Khớp đúng segment path: /share ≠ /shared-with-me. */
    private static boolean hasPathSegment(String path, String segment) {
        for (String part : path.split("/")) {
            if (part.equalsIgnoreCase(segment)) return true;
        }
        return false;
    }

    /** classroom-service ghi log chi tiết kèm lý do, trạng thái trước/sau và IP. */
    private static boolean isClassroomStatusMutation(String path) {
        return path.matches("^/api/(admin/)?classrooms/\\d+/(archive|restore|permanent|lock|unlock)/?$");
    }

    /** /api/classrooms/{id}/posts[...] nhưng không phải /comments (comments vẫn log qua gateway). */
    private static boolean isClassroomPostMutation(String path) {
        if (!path.contains("/classrooms/") || !path.contains("/posts")) return false;
        if (path.contains("/comments")) return false;
        return true;
    }

    /** Mời/thu hồi lời mời: classroom-service ghi log kèm tên lớp. */
    private static boolean isClassroomInviteMutation(String path) {
        return path.contains("/classrooms/")
                && (path.contains("/invite") || path.contains("/invitations"));
    }

    /** Chia sẻ bài giảng vào lớp: lesson-service ghi log kèm tên lớp. */
    private static boolean isLessonClassroomShareMutation(String path) {
        return path.contains("/classroom-shares");
    }

    /** Upload trung gian canvas (save-blob / upload-image) — chỉ log khi /save vào thư viện. */
    private static boolean isCanvasIntermediateUpload(String path) {
        return path.contains("/canvas/upload-image")
                || path.contains("/canvas/upload-audio")
                || path.contains("/canvas/save-blob")
                || path.contains("/api/image/process");
    }

    /** POST /api/tests, PUT|DELETE /api/tests/{id} — không log generic CREATE_TESTS. */
    private static boolean isTestRootMutation(HttpMethod method, String path) {
        if (path == null) return false;
        if (method == HttpMethod.POST && (path.equals("/api/tests") || path.equals("/api/tests/"))) return true;
        if ((method == HttpMethod.PUT || method == HttpMethod.DELETE)
                && path.matches("^/api/tests/\\d+/?$")) return true;
        return false;
    }

    private void send(ServerWebExchange exchange, HttpMethod method, String path, long startedAt, Throwable error) {
        String purpose = exchange.getRequest().getHeaders().getFirst("X-Action-Purpose");
        ActionLogClassifier.Classification classification = ActionLogClassifier.classify(method, path, purpose);
        int statusCode = exchange.getResponse().getStatusCode() == null ? 500 : exchange.getResponse().getStatusCode().value();
        boolean failed = error != null || statusCode >= 400;
        String action = failed && "LOGIN".equals(classification.action()) ? "LOGIN_FAILED" : classification.action();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("authToken", resolveAuthToken(exchange));
        payload.put("clientIdentifier", clientIdentifier(exchange));
        payload.put("action", action);
        payload.put("module", classification.module());
        payload.put("resourceId", classification.resourceId());
        payload.put("httpMethod", method.name());
        payload.put("endpoint", path);
        payload.put("severity", failed && "LOGIN_FAILED".equals(action) ? "ALERT" : classification.severity());
        payload.put("status", failed ? "FAILED" : "SUCCESS");
        payload.put("description", "{\"statusCode\":" + statusCode + ",\"durationMs\":" + (System.currentTimeMillis() - startedAt) + "}");
        payload.put("ipAddress", clientIp(exchange));

        webClient.post().uri("/api/internal/action-logs").bodyValue(payload).retrieve().toBodilessEntity()
                .doOnError(exception -> log.warn("Không thể ghi action log cho {} {}: {}", method, path, exception.getMessage()))
                .onErrorResume(exception -> Mono.empty()).subscribe();
    }

    private String resolveAuthToken(ServerWebExchange exchange) {
        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization != null && !authorization.isBlank()) return authorization;
        // Fallback cookie (credentials: include)
        String cookie = exchange.getRequest().getHeaders().getFirst(HttpHeaders.COOKIE);
        if (cookie == null || cookie.isBlank()) return null;
        for (String part : cookie.split(";")) {
            String item = part.trim();
            int eq = item.indexOf('=');
            if (eq <= 0) continue;
            String name = item.substring(0, eq).trim();
            String value = item.substring(eq + 1).trim();
            if (name.equalsIgnoreCase("token") || name.equalsIgnoreCase("access_token")
                    || name.equalsIgnoreCase("jwt") || name.equalsIgnoreCase("Authorization")) {
                if (value.isBlank()) continue;
                return value.regionMatches(true, 0, "Bearer ", 0, 7) ? value : "Bearer " + value;
            }
        }
        return null;
    }

    private String clientIp(ServerWebExchange exchange) {
        String forwarded = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
        return exchange.getRequest().getRemoteAddress() == null ? null
                : exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
    }

    private String clientIdentifier(ServerWebExchange exchange) {
        String ip = clientIp(exchange);
        String userAgent = exchange.getRequest().getHeaders().getFirst(HttpHeaders.USER_AGENT);
        if (ip != null) return "guest_" + ip;
        if (userAgent == null || userAgent.isBlank()) return "unknown_client";
        return "unknown_" + userAgent.substring(0, Math.min(userAgent.length(), 220));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
