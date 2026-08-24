package vn.edu.primary.speechrecognition.controller;

import vn.edu.primary.speechrecognition.dto.ApiResponse;
import vn.edu.primary.speechrecognition.service.PronunciationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

@RestController
@RequestMapping("")
@CrossOrigin("*")
public class PronunciationController {

    @Autowired
    private PronunciationService pronunciationService;

    private ResponseEntity<?> authenticationRequired() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Authentication is required"));
    }

    private boolean isStudent(HttpServletRequest request) {
        Object roles = request.getAttribute("roles");
        return roles instanceof Set<?> roleNames && roleNames.stream()
                .anyMatch(roleName -> "STUDENT".equalsIgnoreCase(String.valueOf(roleName)));
    }

    private ResponseEntity<?> checkWithWhisper(String targetText, MultipartFile audioFile) {
        try {
            Map<String, Object> result = pronunciationService.checkPronunciation(targetText, audioFile);
            return ResponseEntity.ok(ApiResponse.success("Kiểm tra phát âm thành công", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid input: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error checking pronunciation: " + e.getMessage()));
        }
    }

    @PostMapping("/check")
    public ResponseEntity<?> checkPronunciation(
            @RequestParam("target_text") String targetText,
            @RequestParam("audio_file") MultipartFile audioFile) {
        return checkWithWhisper(targetText, audioFile);
    }

    @PostMapping("/practice/check")
    public ResponseEntity<?> checkStudentPractice(
            @RequestParam("target_text") String targetText,
            @RequestParam("audio_file") MultipartFile audioFile,
            HttpServletRequest request) {
        if (request.getAttribute("userId") == null) {
            return authenticationRequired();
        }
        if (!isStudent(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Student permission is required"));
        }
        return checkWithWhisper(targetText, audioFile);
    }

    @PostMapping("/check-vosk")
    public ResponseEntity<?> checkPronunciationWithVosk(
            @RequestParam("target_text") String targetText,
            @RequestParam("audio_file") MultipartFile audioFile) {
        try {
            Map<String, Object> result = pronunciationService.checkPronunciationWithVosk(targetText, audioFile);
            return ResponseEntity.ok(ApiResponse.success("Kiểm tra phát âm bằng Vosk thành công", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid input: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error checking pronunciation with Vosk: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "speech-recognition-service");
        return ResponseEntity.ok(response);
    }
}