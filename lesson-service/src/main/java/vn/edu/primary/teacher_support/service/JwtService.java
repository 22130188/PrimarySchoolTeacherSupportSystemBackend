package vn.edu.primary.teacher_support.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.List;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return extractAllClaims(token).get("roles", List.class);
    }

    public String extractPrimaryRole(String token) {
        List<String> roles = extractRoles(token);
        if (roles == null || roles.isEmpty()) return null;
        if (roles.contains("ADMIN")) return "ADMIN";
        if (roles.contains("TEACHER")) return "TEACHER";
        if (roles.contains("STUDENT")) return "STUDENT";
        return roles.get(0);
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }
}
