package vn.edu.primary.teacher_support.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.primary.teacher_support.entity.Role;
import vn.edu.primary.teacher_support.entity.User;
import vn.edu.primary.teacher_support.repository.UserRepository;

import java.util.*;

/**
 * Internal API for service-to-service communication.
 * Called by classroom-service to look up users by email/id.
 */
@RestController
@RequestMapping("/api/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserRepository userRepository;

    @GetMapping("/by-email")
    public ResponseEntity<?> findByEmail(@RequestParam String email) {
        Optional<User> userOpt = userRepository.findByEmail(email.trim().toLowerCase());
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toInternalDto(userOpt.get()));
    }

    @PostMapping("/by-emails")
    public ResponseEntity<List<Map<String, Object>>> findByEmails(@RequestBody List<String> emails) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (String email : emails) {
            userRepository.findByEmail(email.trim().toLowerCase())
                    .ifPresent(user -> result.add(toInternalDto(user)));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toInternalDto(userOpt.get()));
    }

    private Map<String, Object> toInternalDto(User user) {
        String primaryRole = user.getRoles().stream()
                .map(Role::getName)
                .max(Comparator.comparingInt(this::rolePriority))
                .map(Enum::name)
                .orElse(null);

        Map<String, Object> dto = new HashMap<>();
        dto.put("id", user.getId());
        dto.put("username", user.getUsername());
        dto.put("email", user.getEmail());
        dto.put("avatarUrl", user.getAvatarUrl());
        dto.put("role", primaryRole);
        dto.put("isActive", user.getIsActive());
        return dto;
    }

    private int rolePriority(Role.RoleName roleName) {
        return switch (roleName) {
            case STUDENT -> 0;
            case TEACHER -> 1;
            case ADMIN -> 2;
        };
    }
}
