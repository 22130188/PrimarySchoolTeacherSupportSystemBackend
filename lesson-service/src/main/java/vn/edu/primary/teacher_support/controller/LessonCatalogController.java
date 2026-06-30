package vn.edu.primary.teacher_support.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.primary.teacher_support.dto.LessonCatalogDto;
import vn.edu.primary.teacher_support.service.AuthHelper;
import vn.edu.primary.teacher_support.service.LessonCatalogService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lessons/catalog")
@RequiredArgsConstructor
public class LessonCatalogController {

    private final LessonCatalogService catalogService;
    private final AuthHelper authHelper;

    @GetMapping
    public ResponseEntity<List<LessonCatalogDto>> getCatalog(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String volume,
            @RequestParam(required = false) String book,
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        return ResponseEntity.ok(catalogService.search(subject, grade, volume, book, activeOnly));
    }

    @GetMapping("/admin")
    public ResponseEntity<List<LessonCatalogDto>> getAdminCatalog(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String volume,
            @RequestParam(required = false) String book) {
        authHelper.validateTeacherOrAdmin(authorization);
        return ResponseEntity.ok(catalogService.search(subject, grade, volume, book, false));
    }

    @PostMapping("/admin")
    public ResponseEntity<LessonCatalogDto> create(
            @RequestHeader("Authorization") String authorization,
            @RequestBody LessonCatalogDto request) {
        authHelper.validateTeacherOrAdmin(authorization);
        Long userId = authHelper.extractUserId(authorization);
        return ResponseEntity.ok(catalogService.create(request, userId));
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<LessonCatalogDto> update(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id,
            @RequestBody LessonCatalogDto request) {
        authHelper.validateTeacherOrAdmin(authorization);
        Long userId = authHelper.extractUserId(authorization);
        return ResponseEntity.ok(catalogService.update(id, request, userId));
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Map<String, String>> delete(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        authHelper.validateTeacherOrAdmin(authorization);
        catalogService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Đã xóa bài học khỏi danh mục"));
    }
}