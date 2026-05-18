package vn.edu.primary.teacher_support.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collections;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import vn.edu.primary.teacher_support.entity.User;
import vn.edu.primary.teacher_support.service.GoogleAuthService;
import vn.edu.primary.teacher_support.service.JwtService;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final GoogleAuthService googleAuthService;
    private final JwtService        jwtService;

    private static final Logger log = LoggerFactory.getLogger(OAuth2SuccessHandler.class);

    private static final String FRONTEND_URL = "http://localhost:5173";

    public OAuth2SuccessHandler(GoogleAuthService googleAuthService,
                                JwtService jwtService) {
        this.googleAuthService = googleAuthService;
        this.jwtService        = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        HttpSession session = request.getSession(false);
        if (session != null) {
            log.info("OAuth2 callback: sessionId={}", session.getId());
            try {
                var names = Collections.list(session.getAttributeNames());
                log.info("Session attributes: {}", names);
            } catch (Exception e) {
                log.warn("Failed to list session attributes", e);
            }
        } else {
            log.warn("OAuth2 callback: no HttpSession found");
        }

        User user;
        try {
            user = googleAuthService.findOrCreateUser(oAuth2User);
            if (user != null) log.info("OAuth2 user processed: id={}, email={}", user.getId(), user.getEmail());
        } catch (Exception ex) {
            log.error("Error processing OAuth2 user", ex);
            throw ex;
        }

        String token = jwtService.generateToken(user);

        var cookie = new jakarta.servlet.http.Cookie(HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_AUTH_REQUEST_COOKIE_NAME, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        response.sendRedirect(FRONTEND_URL + "/oauth2/callback?token=" + token);
    }
}