package vn.edu.primary.teacher_support.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.primary.teacher_support.dto.DraftResponse;
import vn.edu.primary.teacher_support.dto.CollaboraEditorSessionResponse;
import vn.edu.primary.teacher_support.dto.LessonTemplateResponse;
import vn.edu.primary.teacher_support.dto.UpdateLessonTemplateRequest;
import vn.edu.primary.teacher_support.entity.enums.LessonTemplateStatus;
import vn.edu.primary.teacher_support.service.AuthHelper;
import vn.edu.primary.teacher_support.service.CollaboraSessionService;
import vn.edu.primary.teacher_support.service.LessonTemplateService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lessons/templates")
@RequiredArgsConstructor
public class LessonTemplateController {

    private final LessonTemplateService templateService;
    private final CollaboraSessionService collaboraSessionService;
    private final AuthHelper authHelper;

    @GetMapping
    public ResponseEntity<List<LessonTemplateResponse>> getTemplates(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String type
    ) {
        authHelper.validateTeacherOrAdmin(authorization);
        return ResponseEntity.ok(templateService.getActiveTemplates(subject, grade, type));
    }

    @PostMapping("/{templateId}/use")
    public ResponseEntity<DraftResponse> useTemplate(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long templateId
    ) {
        authHelper.validateTeacherOrAdmin(authorization);
        Long userId = authHelper.extractUserId(authorization);
        return ResponseEntity.ok(templateService.useTemplate(userId, templateId));
    }

    @GetMapping("/{templateId}/editor")
    public ResponseEntity<CollaboraEditorSessionResponse> getTemplateEditorSession(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long templateId
    ) {
        authHelper.validateTeacherOrAdmin(authorization);
        Long userId = authHelper.extractUserId(authorization);
        return ResponseEntity.ok(collaboraSessionService.getTemplateEditorSession(userId, templateId));
    }

    @GetMapping("/admin")
    public ResponseEntity<List<LessonTemplateResponse>> getAdminTemplates(
            @RequestHeader("Authorization") String authorization
    ) {
        authHelper.validateAdmin(authorization);
        return ResponseEntity.ok(templateService.getAdminTemplates());
    }

    @PostMapping(value = "/admin/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LessonTemplateResponse> uploadTemplate(
            @RequestHeader("Authorization") String authorization,
            @RequestParam("file") MultipartFile file,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam String subject,
            @RequestParam String grade,
            @RequestParam(required = false) LessonTemplateStatus status
    ) {
        authHelper.validateAdmin(authorization);
        Long adminUserId = authHelper.extractUserId(authorization);
        return ResponseEntity.ok(templateService.uploadTemplate(
                adminUserId,
                file,
                title,
                description,
                subject,
                grade,
                status
        ));
    }

    @PatchMapping("/admin/{templateId}")
    public ResponseEntity<LessonTemplateResponse> updateTemplate(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long templateId,
            @RequestBody UpdateLessonTemplateRequest request
    ) {
        authHelper.validateAdmin(authorization);
        return ResponseEntity.ok(templateService.updateTemplate(templateId, request));
    }

    @DeleteMapping("/admin/{templateId}")
    public ResponseEntity<Map<String, String>> deleteTemplate(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long templateId
    ) {
        authHelper.validateAdmin(authorization);
        templateService.deleteTemplate(templateId);
        return ResponseEntity.ok(Map.of("message", "Da xoa mau bai giang thanh cong"));
    }
}
