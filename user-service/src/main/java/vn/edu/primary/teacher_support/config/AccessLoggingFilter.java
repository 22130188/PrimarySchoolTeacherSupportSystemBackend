package vn.edu.primary.teacher_support.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import vn.edu.primary.teacher_support.dto.ActionLogCreateRequest;
import vn.edu.primary.teacher_support.entity.User;
import vn.edu.primary.teacher_support.repository.UserRepository;
import vn.edu.primary.teacher_support.service.AccessLogService;
import vn.edu.primary.teacher_support.service.ActionLogService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class AccessLoggingFilter extends OncePerRequestFilter {
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final AccessLogService accessLogService;
    private final ActionLogService actionLogService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!"/api/auth/login".equals(request.getRequestURI()) || !"POST".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }
        ContentCachingRequestWrapper wrapped = new ContentCachingRequestWrapper(request);
        try {
            chain.doFilter(wrapped, response);
        } finally {
            String username = readUsername(wrapped);
            User user = username == null ? null : userRepository.findByUsername(username).orElse(null);
            boolean success = response.getStatus() < 400;
            accessLogService.recordLogin(username, user, success, request);

            ActionLogCreateRequest actionLog = new ActionLogCreateRequest();
            actionLog.setUsername(username);
            actionLog.setClientIdentifier("guest_" + request.getRemoteAddr());
            actionLog.setAction(success ? "LOGIN" : "LOGIN_FAILED");
            actionLog.setModule("auth");
            actionLog.setHttpMethod(request.getMethod());
            actionLog.setEndpoint(request.getRequestURI());
            actionLog.setSeverity(success ? "INFO" : "ALERT");
            actionLog.setStatus(success ? "SUCCESS" : "FAILED");
            actionLog.setDescription("{\"statusCode\":" + response.getStatus() + "}");
            actionLog.setIpAddress(request.getRemoteAddr());
            actionLogService.createAsync(actionLog);
        }
    }

    private String readUsername(ContentCachingRequestWrapper request) {
        try {
            String body = new String(request.getContentAsByteArray(), StandardCharsets.UTF_8);
            JsonNode json = objectMapper.readTree(body);
            return json.path("username").asText(null);
        } catch (Exception ignored) {
            return null;
        }
    }
}
