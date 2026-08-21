package vn.edu.primary.tts.controller;

import vn.edu.primary.tts.dto.TTSConvertRequest;
import vn.edu.primary.tts.dto.SaveAudioRequest;
import vn.edu.primary.tts.dto.AudioRecordResponse;
import vn.edu.primary.tts.dto.ApiResponse;
import vn.edu.primary.tts.service.TTSService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("")
@CrossOrigin("*")
public class TTSController {

    @Autowired
    private TTSService ttsService;

    private Long authenticatedUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        return userId instanceof Long ? (Long) userId : null;
    }

    private String authenticatedUserName(HttpServletRequest request) {
        Object username = request.getAttribute("username");
        return username instanceof String ? (String) username : null;
    }

    private ResponseEntity<?> authenticationRequired() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Authentication is required"));
    }

    private boolean isAdministrator(HttpServletRequest request) {
        Object roles = request.getAttribute("roles");
        return roles instanceof Set<?> roleNames && roleNames.stream()
                .anyMatch(roleName -> "ADMIN".equalsIgnoreCase(String.valueOf(roleName)));
    }

    
    @PostMapping("/convert")
    public ResponseEntity<?> convertTextToSpeech(
            @Valid @RequestBody TTSConvertRequest request,
            HttpServletRequest httpRequest) {
        try {
            String audioUrl = ttsService.convertTextToSpeech(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("audioUrl", audioUrl);
            response.put("text", request.getText());
            
            return ResponseEntity.ok(ApiResponse.success("Chuyển đổi thành công", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Lỗi chuyển đổi: " + e.getMessage()));
        }
    }
    
    @PostMapping("/save")
    public ResponseEntity<?> saveAudio(
            @Valid @RequestBody SaveAudioRequest request,
            HttpServletRequest httpRequest) {
        Long userId = authenticatedUserId(httpRequest);
        if (userId == null) {
            return authenticationRequired();
        }

        request.setUserId(userId);
        String username = authenticatedUserName(httpRequest);
        if (username != null && !username.isBlank()) {
            request.setUserName(username);
        }

        try {
            AudioRecordResponse result = ttsService.saveAudio(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Audio saved successfully", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Unable to save audio: " + e.getMessage()));
        }
    }
    @PostMapping("/upload")
    public ResponseEntity<?> uploadAudio(
            @RequestParam("file") MultipartFile file,
            @RequestParam("audioName") String audioName,
            @RequestParam("subject") String subject,
            HttpServletRequest httpRequest) {
        Long userId = authenticatedUserId(httpRequest);
        if (userId == null) {
            return authenticationRequired();
        }

        try {
            String userName = authenticatedUserName(httpRequest);
            AudioRecordResponse result = ttsService.uploadAndSaveAudio(
                    file, audioName, subject, userId, userName == null ? "Unknown" : userName);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Audio uploaded successfully", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Unable to upload audio: " + e.getMessage()));
        }
    }
    @GetMapping("/admin/audios")
    public ResponseEntity<?> getAllAudiosForAdmin(HttpServletRequest httpRequest) {
        if (authenticatedUserId(httpRequest) == null) {
            return authenticationRequired();
        }
        if (!isAdministrator(httpRequest)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Administrator permission is required"));
        }

        try {
            List<AudioRecordResponse> audios = ttsService.getAllAudios();
            return ResponseEntity.ok(ApiResponse.success("Audio library loaded", audios));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Unable to load audio library: " + e.getMessage()));
        }
    }

    @GetMapping("/audios/{userId}")
    public ResponseEntity<?> getUserAudios(@PathVariable Long userId, HttpServletRequest httpRequest) {
        Long authenticatedUserId = authenticatedUserId(httpRequest);
        if (authenticatedUserId == null) {
            return authenticationRequired();
        }
        if (!authenticatedUserId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("You can only access your own audio library"));
        }

        try {
            List<AudioRecordResponse> audios = ttsService.getUserAudios(authenticatedUserId);
            return ResponseEntity.ok(ApiResponse.success("Audio library loaded", audios));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Unable to load audio library: " + e.getMessage()));
        }
    }
    @GetMapping("/audios")
    public ResponseEntity<?> getCurrentUserAudios(HttpServletRequest httpRequest) {
        Long userId = authenticatedUserId(httpRequest);
        if (userId == null) {
            return authenticationRequired();
        }

        try {
            List<AudioRecordResponse> audios = ttsService.getUserAudios(userId);
            return ResponseEntity.ok(ApiResponse.success("Audio library loaded", audios));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Unable to load audio library: " + e.getMessage()));
        }
    }
    @DeleteMapping("/admin/audios/{audioId}")
    public ResponseEntity<?> deleteAudioForAdmin(@PathVariable Long audioId, HttpServletRequest httpRequest) {
        if (authenticatedUserId(httpRequest) == null) {
            return authenticationRequired();
        }
        if (!isAdministrator(httpRequest)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Administrator permission is required"));
        }

        try {
            ttsService.deleteAudio(audioId);
            return ResponseEntity.ok(ApiResponse.success("Audio deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Unable to delete audio: " + e.getMessage()));
        }
    }

    @DeleteMapping("/audios/{audioId}")
    public ResponseEntity<?> deleteAudio(@PathVariable Long audioId, HttpServletRequest httpRequest) {
        Long userId = authenticatedUserId(httpRequest);
        if (userId == null) {
            return authenticationRequired();
        }

        boolean ownsAudio = ttsService.getUserAudios(userId).stream()
                .anyMatch(audio -> audioId.equals(audio.getId()));
        if (!ownsAudio) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Audio record was not found"));
        }

        try {
            ttsService.deleteAudio(audioId);
            return ResponseEntity.ok(ApiResponse.success("Audio deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Unable to delete audio: " + e.getMessage()));
        }
    }
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(ApiResponse.success("TTS Service is running", null));
    }
}
