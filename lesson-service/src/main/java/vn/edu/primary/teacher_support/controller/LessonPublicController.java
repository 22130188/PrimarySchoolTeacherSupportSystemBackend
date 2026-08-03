package vn.edu.primary.teacher_support.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.primary.teacher_support.dto.*;
import vn.edu.primary.teacher_support.entity.enums.PublicReportStatus;
import vn.edu.primary.teacher_support.exception.ForbiddenException;
import vn.edu.primary.teacher_support.service.AuthHelper;
import vn.edu.primary.teacher_support.service.CollaboraSessionService;
import vn.edu.primary.teacher_support.service.LessonPublicService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lessons/public")
@RequiredArgsConstructor
public class LessonPublicController {

    private final LessonPublicService publicService;
    private final CollaboraSessionService collaboraSessionService;
    private final AuthHelper authHelper;

    private Long requireTeacherOrAdmin(String authorization) {
        authHelper.validateTeacherOrAdmin(authorization);
        return authHelper.extractUserId(authorization);
    }

    private void requireAdmin(String authorization) {
        authHelper.validateAdmin(authorization);
    }

    @GetMapping("/config/verification")
    public ResponseEntity<PublicVerificationConfigDto> getConfig(
            @RequestHeader("Authorization") String authorization) {
        requireTeacherOrAdmin(authorization);
        return ResponseEntity.ok(publicService.getConfigDto());
    }

    @GetMapping
    public ResponseEntity<List<PublicLessonResponse>> list(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String verificationStatus) {
        Long userId = requireTeacherOrAdmin(authorization);
        return ResponseEntity.ok(publicService.listPublic(userId, subject, grade, type, keyword, verificationStatus));
    }

    @GetMapping("/{draftId}")
    public ResponseEntity<PublicLessonResponse> getOne(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long draftId) {
        Long userId = requireTeacherOrAdmin(authorization);
        return ResponseEntity.ok(publicService.getPublicLesson(draftId, userId, true));
    }

    @GetMapping("/{draftId}/editor")
    public ResponseEntity<CollaboraEditorSessionResponse> getEditor(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long draftId) {
        Long userId = requireTeacherOrAdmin(authorization);
        // Ensure public access
        publicService.getPublicLesson(draftId, userId, false);
        return ResponseEntity.ok(collaboraSessionService.getPublicEditorSession(userId, draftId));
    }

    /** Owner-only: public toggle status for Share modal. */
    @GetMapping("/{draftId}/status")
    public ResponseEntity<PublicLessonResponse> status(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long draftId) {
        Long userId = requireTeacherOrAdmin(authorization);
        return ResponseEntity.ok(publicService.getStatus(draftId, userId));
    }

    /** Owner-only: turn public ON (always UNVERIFIED first). */
    @PostMapping("/{draftId}/publish")
    public ResponseEntity<PublicLessonResponse> publish(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long draftId) {
        Long userId = requireTeacherOrAdmin(authorization);
        return ResponseEntity.ok(publicService.publish(draftId, userId));
    }

    /** Owner-only: turn public OFF. */
    @DeleteMapping("/{draftId}/publish")
    public ResponseEntity<PublicLessonResponse> unpublish(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long draftId) {
        Long userId = requireTeacherOrAdmin(authorization);
        return ResponseEntity.ok(publicService.unpublish(draftId, userId));
    }

    @PostMapping("/{draftId}/copy")
    public ResponseEntity<DraftResponse> copy(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long draftId) {
        Long userId = requireTeacherOrAdmin(authorization);
        return ResponseEntity.ok(publicService.copyToMyLessons(draftId, userId));
    }

    @PutMapping("/{draftId}/ratings")
    public ResponseEntity<PublicLessonResponse> rate(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long draftId,
            @Valid @RequestBody RatePublicLessonRequest request) {
        Long userId = requireTeacherOrAdmin(authorization);
        return ResponseEntity.ok(publicService.rate(draftId, userId, request.getStars()));
    }

    @GetMapping("/{draftId}/ratings/me")
    public ResponseEntity<Map<String, Object>> myRating(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long draftId) {
        Long userId = requireTeacherOrAdmin(authorization);
        return ResponseEntity.ok(publicService.getMyRating(draftId, userId));
    }

    @PostMapping("/{draftId}/reports")
    public ResponseEntity<PublicReportResponse> report(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long draftId,
            @Valid @RequestBody ReportPublicLessonRequest request) {
        Long userId = requireTeacherOrAdmin(authorization);
        return ResponseEntity.ok(publicService.report(draftId, userId, request.getReason(), request.getDetail()));
    }

    // ── Admin ──────────────────────────────────────────────

    @GetMapping("/admin/lessons")
    public ResponseEntity<List<PublicLessonResponse>> adminLessons(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(required = false) Boolean isPublic,
            @RequestParam(required = false) String verificationStatus) {
        requireAdmin(authorization);
        return ResponseEntity.ok(publicService.listAdminLessons(isPublic, verificationStatus));
    }

    @GetMapping("/admin/reports")
    public ResponseEntity<List<PublicReportResponse>> adminReports(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(required = false) String status) {
        requireAdmin(authorization);
        return ResponseEntity.ok(publicService.listAdminReports(status));
    }

    @PatchMapping("/admin/reports/{reportId}")
    public ResponseEntity<PublicReportResponse> resolveReport(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long reportId,
            @Valid @RequestBody ResolvePublicReportRequest request) {
        requireAdmin(authorization);
        PublicReportStatus status = request.getStatus();
        if (status == null) throw new ForbiddenException("Thiếu trạng thái");
        return ResponseEntity.ok(publicService.resolveReport(reportId, status, request.getAdminNote()));
    }

    @PostMapping("/admin/lessons/{draftId}/unpublish")
    public ResponseEntity<PublicLessonResponse> adminUnpublish(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long draftId,
            @RequestBody(required = false) AdminUnpublishRequest request) {
        requireAdmin(authorization);
        String reason = request != null ? request.getReason() : null;
        return ResponseEntity.ok(publicService.adminUnpublish(draftId, reason));
    }

    @PutMapping("/admin/config/verification")
    public ResponseEntity<PublicVerificationConfigDto> updateConfig(
            @RequestHeader("Authorization") String authorization,
            @RequestBody PublicVerificationConfigDto body) {
        requireAdmin(authorization);
        return ResponseEntity.ok(publicService.updateConfig(body));
    }

    @PostMapping("/admin/lessons/{draftId}/reevaluate")
    public ResponseEntity<PublicLessonResponse> reevaluate(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long draftId) {
        requireAdmin(authorization);
        return ResponseEntity.ok(publicService.reevaluate(draftId));
    }
}
