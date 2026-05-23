package vn.edu.primary.teacher_support.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.primary.teacher_support.dto.*;
import vn.edu.primary.teacher_support.service.AuthHelper;
import vn.edu.primary.teacher_support.service.LessonShareService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonShareController {

    private final LessonShareService shareService;
    private final AuthHelper authHelper;

    @PostMapping("/drafts/{id}/shares")
    public ResponseEntity<ShareResponse> shareDraft(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id,
            @Valid @RequestBody ShareLessonRequest request) {
        Long userId = authHelper.extractUserId(authorization);
        ShareResponse response = shareService.shareDraft(id, userId, request.getEmail(), request.getPermission());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/drafts/{id}/shares")
    public ResponseEntity<List<ShareResponse>> getSharesForDraft(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        Long userId = authHelper.extractUserId(authorization);
        return ResponseEntity.ok(shareService.getSharesForDraft(id, userId));
    }

    @PatchMapping("/drafts/{id}/shares/{targetUserId}")
    public ResponseEntity<ShareResponse> updateSharePermission(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id,
            @PathVariable Long targetUserId,
            @Valid @RequestBody UpdateSharePermissionRequest request) {
        Long userId = authHelper.extractUserId(authorization);
        return ResponseEntity.ok(shareService.updatePermission(id, targetUserId, userId, request.getPermission()));
    }

    @DeleteMapping("/drafts/{id}/shares/{targetUserId}")
    public ResponseEntity<Map<String, String>> revokeShare(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id,
            @PathVariable Long targetUserId) {
        Long userId = authHelper.extractUserId(authorization);
        shareService.revokeShare(id, targetUserId, userId);
        return ResponseEntity.ok(Map.of("message", "Đã thu hồi chia sẻ thành công"));
    }

    @GetMapping("/shared-with-me")
    public ResponseEntity<List<SharedDraftResponse>> getSharedWithMe(
            @RequestHeader("Authorization") String authorization) {
        Long userId = authHelper.extractUserId(authorization);
        return ResponseEntity.ok(shareService.getSharedWithMe(userId));
    }

    @GetMapping("/shared-with-me/{id}")
    public ResponseEntity<SharedDraftResponse> getSharedDraft(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        Long userId = authHelper.extractUserId(authorization);
        return ResponseEntity.ok(shareService.getSharedDraft(id, userId));
    }

    @PostMapping("/shared-with-me/{id}/duplicate")
    public ResponseEntity<Map<String, Object>> duplicateSharedDraft(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        Long userId = authHelper.extractUserId(authorization);
        Long newDraftId = shareService.duplicateSharedDraft(id, userId);
        return ResponseEntity.ok(Map.of("message", "Đã tạo bản sao thành công", "newDraftId", newDraftId));
    }
}
