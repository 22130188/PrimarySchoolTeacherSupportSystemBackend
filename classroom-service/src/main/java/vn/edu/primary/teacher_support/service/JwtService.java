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

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("roles", List.class);
    }

    public String extractPrimaryRole(String token) {
        List<String> roles = extractRoles(token);
        if (roles != null && !roles.isEmpty()) {
            // Normalize ROLE_ADMIN / Admin → ADMIN
            java.util.List<String> normalized = roles.stream()
                    .filter(r -> r != null && !r.isBlank())
                    .map(r -> r.trim().toUpperCase().replace("ROLE_", ""))
                    .toList();
            if (normalized.contains("ADMIN")) return "ADMIN";
            if (normalized.contains("TEACHER")) return "TEACHER";
            if (normalized.contains("STUDENT")) return "STUDENT";
            return normalized.get(0);
        }
        // Fallback: single "role" claim if present
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
