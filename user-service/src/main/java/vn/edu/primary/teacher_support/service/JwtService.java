package vn.edu.primary.teacher_support.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.edu.primary.teacher_support.entity.User;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private Key getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(User user) {
        // Prefer user_roles join table; fall back to users.role column (many admin rows only set role)
        java.util.LinkedHashSet<String> roleNames = new java.util.LinkedHashSet<>();
        if (user.getRoles() != null) {
            user.getRoles().stream()
                    .filter(r -> r != null && r.getName() != null)
                    .map(r -> r.getName().name())
                    .forEach(roleNames::add);
        }
        if (user.getRole() != null) {
            roleNames.add(user.getRole().name());
        }
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getId())
                .claim("email", user.getEmail())
                .claim("roles", roleNames.stream().toList())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey()).build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public Long extractUserId(String token) {
        try {
            Object value = Jwts.parserBuilder()
                    .setSigningKey(getKey()).build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("userId");
            if (value instanceof Number number) return number.longValue();
            if (value != null) return Long.parseLong(value.toString());
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isValid(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}