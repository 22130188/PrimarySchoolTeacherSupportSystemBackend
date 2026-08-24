package vn.edu.primary.speechrecognition.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtProvider {

    @Value("${jwt.secret-key}")
    private String secretKey;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public Long extractUserId(String token) {
        Object userId = getAllClaimsFromToken(token).get("userId");
        return userId instanceof Number ? ((Number) userId).longValue() : null;
    }

    public Set<String> extractRoles(String token) {
        Object rolesClaim = getAllClaimsFromToken(token).get("roles");
        if (!(rolesClaim instanceof Collection<?> roles)) {
            return Collections.emptySet();
        }
        return roles.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .collect(Collectors.toSet());
    }

    public boolean validateToken(String token) {
        try {
            getAllClaimsFromToken(token);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}