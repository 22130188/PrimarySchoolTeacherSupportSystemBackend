package vn.edu.primary.teacher_support.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.primary.teacher_support.dto.AdminDraftResponse;
import vn.edu.primary.teacher_support.dto.DraftResponse;
import vn.edu.primary.teacher_support.dto.SaveDraftRequest;
import vn.edu.primary.teacher_support.dto.UpdateDraftMetadataRequest;
import vn.edu.primary.teacher_support.dto.UpdateDraftStatusRequest;
import vn.edu.primary.teacher_support.service.AuthHelper;
import vn.edu.primary.teacher_support.service.LessonDraftService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lessons/drafts")
@RequiredArgsConstructor
public class LessonDraftController {

    private final LessonDraftService draftService;
    private final AuthHelper authHelper;

    @PostMapping
    public ResponseEntity<DraftResponse> saveDraft(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody SaveDraftRequest request) {
        Long userId = authHelper.extractUserId(authorization);
        DraftResponse response = draftService.saveDraft(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<DraftResponse>> getDrafts(
            @RequestHeader("Authorization") String authorization) {
        Long userId = authHelper.extractUserId(authorization);
        return ResponseEntity.ok(draftService.getDrafts(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<DraftResponse>> searchDrafts(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String grade) {
        Long userId = authHelper.extractUserId(authorization);
        return ResponseEntity.ok(draftService.searchDrafts(userId, title, subject, grade));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DraftResponse> getDraft(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        Long userId = authHelper.extractUserId(authorization);
        return ResponseEntity.ok(draftService.getDraft(id, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteDraft(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        Long userId = authHelper.extractUserId(authorization);
        draftService.deleteDraft(id, userId);
        return ResponseEntity.ok(Map.of("message", "Đã xóa bản nháp thành công"));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<DraftResponse> updateStatus(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id,
            @Valid @RequestBody UpdateDraftStatusRequest request) {
        Long userId = authHelper.extractUserId(authorization);
        return ResponseEntity.ok(draftService.updateStatus(id, userId, request.getStatus()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DraftResponse> updateMetadata(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id,
            @Valid @RequestBody UpdateDraftMetadataRequest request) {
        Long userId = authHelper.extractUserId(authorization);
        return ResponseEntity.ok(draftService.updateMetadata(id, userId, request));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<AdminDraftResponse>> getAllDraftsForAdmin(
            @RequestHeader("Authorization") String authorization) {
        authHelper.validateTeacherOrAdmin(authorization);
        return ResponseEntity.ok(draftService.getAllDraftsForAdmin());
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Map<String, String>> deleteDraftForAdmin(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        authHelper.validateTeacherOrAdmin(authorization);
        draftService.deleteDraftForAdmin(id);
        return ResponseEntity.ok(Map.of("message", "Đã xóa bài giảng thành công"));
    }
}
