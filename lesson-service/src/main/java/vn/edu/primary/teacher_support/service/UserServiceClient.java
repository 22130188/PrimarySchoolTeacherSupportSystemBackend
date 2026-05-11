package vn.edu.primary.teacher_support.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import vn.edu.primary.teacher_support.dto.UserDto;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {

    private final RestTemplate restTemplate;
    private static final String USER_SERVICE_URL = "http://user-service";

    public Optional<UserDto> findByEmail(String email) {
        try {
            ResponseEntity<UserDto> response = restTemplate.getForEntity(
                    USER_SERVICE_URL + "/api/internal/users/by-email?email=" + email,
                    UserDto.class);
            return Optional.ofNullable(response.getBody());
        } catch (Exception e) {
            log.warn("Failed to fetch user by email {}: {}", email, e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<UserDto> findById(Long id) {
        try {
            ResponseEntity<UserDto> response = restTemplate.getForEntity(
                    USER_SERVICE_URL + "/api/internal/users/" + id,
                    UserDto.class);
            return Optional.ofNullable(response.getBody());
        } catch (Exception e) {
            log.warn("Failed to fetch user by id {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }
}
