package vn.edu.primary.teacher_support.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

        // Tìm hoặc tạo user trong DB
        User user = googleAuthService.findOrCreateUser(oAuth2User);

        // Tạo JWT token
        String token = jwtService.generateToken(user);

        // Redirect về frontend kèm token trong URL
        // Frontend sẽ đọc token từ URL param và lưu vào localStorage
        response.sendRedirect(FRONTEND_URL + "/oauth2/callback?token=" + token);
    }
}