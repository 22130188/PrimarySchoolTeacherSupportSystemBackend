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

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractEmail(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("email", String.class);
    }

    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        Object raw = claims.get("roles");
        if (raw instanceof List<?> list) {
            return list.stream()
                    .filter(java.util.Objects::nonNull)
                    .map(Object::toString)
                    .toList();
        }
        if (raw instanceof String s && !s.isBlank()) {
            return List.of(s);
        }
        return List.of();
    }

    public String extractPrimaryRole(String token) {
        List<String> roles = extractRoles(token);
        if (!roles.isEmpty()) {
            java.util.List<String> normalized = roles.stream()
                    .filter(r -> r != null && !r.isBlank())
                    .map(r -> r.trim().toUpperCase().replace("ROLE_", ""))
                    .toList();
            if (normalized.contains("ADMIN")) return "ADMIN";
            if (normalized.contains("TEACHER")) return "TEACHER";
            if (normalized.contains("STUDENT")) return "STUDENT";
            return normalized.get(0);
        }
        Claims claims = extractAllClaims(token);
        Object single = claims.get("role");
        if (single != null) {
            String r = single.toString().trim().toUpperCase().replace("ROLE_", "");
            if (!r.isBlank()) return r;
        }
        return null;
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }
}
