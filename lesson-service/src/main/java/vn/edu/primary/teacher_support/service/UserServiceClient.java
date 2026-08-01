package vn.edu.primary.teacher_support.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import vn.edu.primary.teacher_support.dto.UserDto;

import java.util.Optional;

@Service
@Slf4j
public class UserServiceClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${user.service.url:http://user-service:8082}")
    private String userServiceUrl;

    public Optional<UserDto> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        try {
            ResponseEntity<UserDto> response = restTemplate.getForEntity(
                    userServiceUrl + "/api/internal/users/by-email?email={email}",
                    UserDto.class,
                    email);
            return Optional.ofNullable(response.getBody());
        } catch (Exception e) {
            log.warn("Failed to fetch user by email {}: {}", email, e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<UserDto> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        try {
            ResponseEntity<UserDto> response = restTemplate.getForEntity(
                    userServiceUrl + "/api/internal/users/{id}",
                    UserDto.class,
                    id);
            return Optional.ofNullable(response.getBody());
        } catch (Exception e) {
            log.warn("Failed to fetch user by id {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }
}
