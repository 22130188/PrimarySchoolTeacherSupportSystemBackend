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
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("")
@CrossOrigin("*")
public class TTSController {

    @Autowired
    private TTSService ttsService;
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
    public ResponseEntity<?> saveAudio(@Valid @RequestBody SaveAudioRequest request) {
        try {
            AudioRecordResponse result = ttsService.saveAudio(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Lưu âm thanh thành công", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Lỗi lưu âm thanh: " + e.getMessage()));
        }
    }
    @GetMapping("/audios/{userId}")
    public ResponseEntity<?> getUserAudios(@PathVariable Long userId) {
        try {
            List<AudioRecordResponse> audios = ttsService.getUserAudios(userId);
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thành công", audios));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Lỗi lấy danh sách: " + e.getMessage()));
        }
    }

    @GetMapping("/audios")
    public ResponseEntity<?> getAllAudios() {
        try {
            List<AudioRecordResponse> audios = ttsService.getAllAudios();
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thành công", audios));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Lỗi lấy danh sách: " + e.getMessage()));
        }
    }
    @DeleteMapping("/audios/{audioId}")
    public ResponseEntity<?> deleteAudio(@PathVariable Long audioId) {
        try {
            ttsService.deleteAudio(audioId);
            return ResponseEntity.ok(ApiResponse.success("Xóa âm thanh thành công", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Lỗi xóa âm thanh: " + e.getMessage()));
        }
    }
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(ApiResponse.success("TTS Service is running", null));
    }
}
