package vn.edu.primary.teacher_support.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import vn.edu.primary.teacher_support.dto.ActionLogCreateRequest;
import vn.edu.primary.teacher_support.service.ActionLogService;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2AuthenticationFailureHandler.class);
    private final ActionLogService actionLogService;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        log.error("OAuth2 login failure: {}", exception.getMessage(), exception);
        ActionLogCreateRequest actionLog = new ActionLogCreateRequest();
        actionLog.setClientIdentifier("guest_" + request.getRemoteAddr());
        actionLog.setAction("LOGIN_FAILED");
        actionLog.setModule("auth");
        actionLog.setHttpMethod("GET");
        actionLog.setEndpoint(request.getRequestURI());
        actionLog.setSeverity("ALERT");
        actionLog.setStatus("FAILED");
        actionLog.setDescription("{\"provider\":\"google\",\"error\":\"oauth2_authentication_failed\"}");
        actionLog.setIpAddress(request.getRemoteAddr());
        actionLogService.createAsync(actionLog);
        String error = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
        var cookie = new jakarta.servlet.http.Cookie(HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_AUTH_REQUEST_COOKIE_NAME, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        String base = frontendUrl == null ? "http://localhost:5173" : frontendUrl.replaceAll("/$", "");
        response.sendRedirect(base + "/oauth2/callback?error=" + error);
    }
}
