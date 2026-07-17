package vn.edu.primary.translate.controller;

import vn.edu.primary.translate.dto.ApiResponse;
import vn.edu.primary.translate.dto.TranslateRequest;
import vn.edu.primary.translate.dto.DocumentTranslateRequest;
import vn.edu.primary.translate.service.TranslateService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("")
@CrossOrigin("*")
public class TranslateController {

    @Autowired
    private TranslateService translateService;

    @PostMapping("/translate")
    public ResponseEntity<?> translateText(@Valid @RequestBody TranslateRequest request) {
        try {
            Map<String, Object> result = translateService.translateText(
                    request.getText(),
                    request.getSourceLang(),
                    request.getTargetLang()
            );
            return ResponseEntity.ok(ApiResponse.success("Dịch thành công", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid input: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi dịch thuật: " + e.getMessage()));
        }
    }

    @PostMapping("/document")
    public ResponseEntity<?> translateDocument(@Valid @RequestBody DocumentTranslateRequest request) {
        try {
            Map<String, Object> result = translateService.translateDocument(
                    request.getText(),
                    request.getSourceLang(),
                    request.getTargetLang()
            );
            return ResponseEntity.ok(ApiResponse.success("Dịch tài liệu thành công", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid input: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi dịch tài liệu: " + e.getMessage()));
        }
    }

    @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> extractText(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = translateService.extractTextFromFile(file);
            return ResponseEntity.ok(ApiResponse.success("Trích xuất thành công", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid input: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi trích xuất tệp: " + e.getMessage()));
        }
    }

    /** Proxy upload → Python /api/translate/document/file → return translated file bytes */
    @PostMapping(value = "/document/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> translateDocumentFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "source_lang", defaultValue = "vi") String sourceLang,
            @RequestParam(value = "target_lang", defaultValue = "en") String targetLang) {
        try {
            TranslateService.FileTranslateResult result =
                    translateService.translateDocumentFile(file, sourceLang, targetLang);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + result.filename() + "\"")
                    .contentType(MediaType.parseMediaType(result.contentType()))
                    .body(result.body());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid input: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi dịch file tài liệu: " + e.getMessage()));
        }
    }

    @GetMapping("/languages")
    public ResponseEntity<?> getLanguages() {
        Map<String, Object> languages = new HashMap<>();
        languages.put("vi", "Tiếng Việt");
        languages.put("en", "English");

        Map<String, Object>[] pairs = new Map[]{
                Map.of("source", "vi", "target", "en", "label", "Việt → Anh"),
                Map.of("source", "en", "target", "vi", "label", "Anh → Việt")
        };

        Map<String, Object> response = new HashMap<>();
        response.put("languages", languages);
        response.put("supported_pairs", pairs);

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách ngôn ngữ thành công", response));
    }

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "translate-service");
        return ResponseEntity.ok(response);
    }
}
