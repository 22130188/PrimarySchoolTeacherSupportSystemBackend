package vn.edu.primary.teacher_support.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.primary.teacher_support.dto.*;
import vn.edu.primary.teacher_support.service.*;

import java.util.List;

@RestController
@RequestMapping("/api/classrooms")
@RequiredArgsConstructor
public class ClassroomController {

    private final ClassroomService classroomService;
    private final InvitationService invitationService;
    private final ExcelImportService excelImportService;
    private final AuthHelper authHelper;

    @PostMapping
    public ResponseEntity<ClassroomResponse> createClassroom(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody CreateClassroomRequest request) {
        authHelper.validateTeacherOrAdmin(authorization);
        Long userId = authHelper.extractUserId(authorization);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(classroomService.createClassroom(request, userId));
    }

    @GetMapping
    public ResponseEntity<List<ClassroomResponse>> getMyClassrooms(
            @RequestHeader("Authorization") String authorization) {
        Long userId = authHelper.extractUserId(authorization);
        return ResponseEntity.ok(classroomService.getMyClassrooms(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassroomResponse> getClassroom(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        Long userId = authHelper.extractUserId(authorization);
        return ResponseEntity.ok(classroomService.getClassroom(id, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClassroomResponse> updateClassroom(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id,
            @Valid @RequestBody UpdateClassroomRequest request) {
        authHelper.validateTeacherOrAdmin(authorization);
        Long userId = authHelper.extractUserId(authorization);
        return ResponseEntity.ok(classroomService.updateClassroom(id, request, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClassroom(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        authHelper.validateTeacherOrAdmin(authorization);
        Long userId = authHelper.extractUserId(authorization);
        classroomService.deleteClassroom(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/invite")
    public ResponseEntity<InvitationResponse> inviteByEmail(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id,
            @Valid @RequestBody InviteEmailRequest request) {
        authHelper.validateTeacherOrAdmin(authorization);
        Long userId = authHelper.extractUserId(authorization);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invitationService.inviteByEmail(id, request.getEmail(), userId));
    }

    @PostMapping("/{id}/import-excel")
    public ResponseEntity<ExcelImportResult> importExcel(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        authHelper.validateTeacherOrAdmin(authorization);
        Long userId = authHelper.extractUserId(authorization);
        return ResponseEntity.ok(excelImportService.importExcel(id, file, userId));
    }

    @GetMapping("/{id}/roster")
    public ResponseEntity<ClassroomRosterResponse> getRoster(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        authHelper.validateTeacherOrAdmin(authorization);
        Long userId = authHelper.extractUserId(authorization);
        return ResponseEntity.ok(classroomService.getRoster(id, userId));
    }

    @GetMapping("/{id}/invitations")
    public ResponseEntity<List<InvitationResponse>> getInvitations(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        authHelper.validateTeacherOrAdmin(authorization);
        return ResponseEntity.ok(invitationService.getInvitations(id));
    }

    @PostMapping("/{id}/invitations/{invId}/resend")
    public ResponseEntity<Void> resendInvitation(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id,
            @PathVariable Long invId) {
        authHelper.validateTeacherOrAdmin(authorization);
        Long userId = authHelper.extractUserId(authorization);
        invitationService.resendInvitation(id, invId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/invitations/{invId}")
    public ResponseEntity<Void> revokeInvitation(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id,
            @PathVariable Long invId) {
        authHelper.validateTeacherOrAdmin(authorization);
        Long userId = authHelper.extractUserId(authorization);
        invitationService.revokeInvitation(id, invId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reset-invite-link")
    public ResponseEntity<ClassroomResponse> resetInviteLink(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        authHelper.validateTeacherOrAdmin(authorization);
        Long userId = authHelper.extractUserId(authorization);
        return ResponseEntity.ok(classroomService.resetInviteLink(id, userId));
    }

    @PostMapping("/{id}/reset-class-code")
    public ResponseEntity<ClassroomResponse> resetClassCode(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        authHelper.validateTeacherOrAdmin(authorization);
        Long userId = authHelper.extractUserId(authorization);
        return ResponseEntity.ok(classroomService.resetClassCode(id, userId));
    }

    @DeleteMapping("/{id}/members/{memberId}")
    public ResponseEntity<Void> removeStudent(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id,
            @PathVariable Long memberId) {
        authHelper.validateTeacherOrAdmin(authorization);
        Long userId = authHelper.extractUserId(authorization);
        classroomService.removeStudent(id, memberId, userId);
        return ResponseEntity.noContent().build();
    }
}
