package vn.edu.primary.teacher_support.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.util.Base64;

public class HttpCookieOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final Logger log = LoggerFactory.getLogger(HttpCookieOAuth2AuthorizationRequestRepository.class);
    public static final String OAUTH2_AUTH_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    public static final int COOKIE_EXPIRE_SECONDS = (int) Duration.ofMinutes(5).getSeconds();

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        Cookie cookie = getCookie(request, OAUTH2_AUTH_REQUEST_COOKIE_NAME);
        if (cookie == null) {
            log.debug("No oauth2_auth_request cookie found");
            return null;
        }
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(cookie.getValue());
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
                OAuth2AuthorizationRequest authRequest = (OAuth2AuthorizationRequest) ois.readObject();
                log.debug("Loaded OAuth2 authorization request from cookie: authorizationUri={}", authRequest.getAuthorizationUri());
                return authRequest;
            }
        } catch (Exception e) {
            log.warn("Failed to load OAuth2AuthorizationRequest from cookie", e);
            return null;
        }
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            removeAuthorizationRequest(request, response);
            return;
        }

        try {
            byte[] bytes;
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                 ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(authorizationRequest);
                oos.flush();
                bytes = bos.toByteArray();
            }
            String encoded = Base64.getUrlEncoder().encodeToString(bytes);
            String cookieValue = encodeCookieValue(encoded);
            response.addHeader("Set-Cookie", cookieValue);
            log.debug("Saved OAuth2 authorization request in cookie: authorizationUri={}", authorizationRequest.getAuthorizationUri());
        } catch (IOException e) {
            log.warn("Failed to save OAuth2AuthorizationRequest to cookie", e);
        }
    }

    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request) {
        return loadAuthorizationRequest(request);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        OAuth2AuthorizationRequest authRequest = loadAuthorizationRequest(request);
        String cookieValue = removeCookieValue();
        response.addHeader("Set-Cookie", cookieValue);
        log.debug("Removed oauth2_auth_request cookie");
        return authRequest;
    }

    private String encodeCookieValue(String value) {
        return OAUTH2_AUTH_REQUEST_COOKIE_NAME + "=" + value + "; Path=/; HttpOnly; Max-Age=" + COOKIE_EXPIRE_SECONDS + "; SameSite=Lax";
    }

    private String removeCookieValue() {
        return OAUTH2_AUTH_REQUEST_COOKIE_NAME + "=; Path=/; HttpOnly; Max-Age=0; SameSite=Lax";
    }

    private static Cookie getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(name)) return cookie;
        }
        return null;
    }
}
