package vn.edu.primary.teacher_support.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2AuthenticationFailureHandler.class);
    private static final String FRONTEND_URL = "http://localhost:5173";

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        log.error("OAuth2 login failure: {}", exception.getMessage(), exception);
        String error = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
        var cookie = new jakarta.servlet.http.Cookie(HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_AUTH_REQUEST_COOKIE_NAME, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        response.sendRedirect(FRONTEND_URL + "/oauth2/callback?error=" + error);
    }
}
