package vn.edu.primary.teacher_support.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.primary.teacher_support.dto.InvitationResponse;
import vn.edu.primary.teacher_support.dto.ResolveAfterRegisterRequest;
import vn.edu.primary.teacher_support.entity.Classroom;
import vn.edu.primary.teacher_support.service.ClassroomService;
import vn.edu.primary.teacher_support.service.InvitationService;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class InternalClassroomController {

    private final InvitationService invitationService;
    private final ClassroomService classroomService;

    @GetMapping("/invitations/by-token/{token}")
    public ResponseEntity<InvitationResponse> getByToken(@PathVariable String token) {
        return ResponseEntity.ok(invitationService.getByToken(token));
    }

    @PostMapping("/invitations/resolve-after-register")
    public ResponseEntity<Map<String, Object>> resolveAfterRegister(
            @RequestBody ResolveAfterRegisterRequest request) {
        int count = invitationService.resolveAfterRegister(request.getEmail(), request.getUserId());
        return ResponseEntity.ok(Map.of(
                "message", "Resolved " + count + " invitations",
                "resolved", count));
    }

    @GetMapping("/classrooms/validate-invite-link/{token}")
    public ResponseEntity<Map<String, Object>> validateInviteLink(@PathVariable String token) {
        try {
            Classroom classroom = classroomService.findByInviteLinkToken(token);
            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "classroomId", classroom.getId(),
                    "classroomName", classroom.getName()));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("valid", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/classrooms/validate-class-code/{code}")
    public ResponseEntity<Map<String, Object>> validateClassCode(@PathVariable String code) {
        try {
            Classroom classroom = classroomService.findByClassCode(code.trim().toUpperCase());
            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "classroomId", classroom.getId(),
                    "classroomName", classroom.getName()));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("valid", false, "message", e.getMessage()));
        }
    }
    @GetMapping("/classrooms/{id}")
    public ResponseEntity<Map<String, Object>> getClassroomById(@PathVariable Long id) {
        try {
            Classroom classroom = classroomService.getActiveClassroom(id);
            return ResponseEntity.ok(Map.of(
                    "id", classroom.getId(),
                    "name", classroom.getName(),
                    "teacherId", classroom.getTeacherId(),
                    "gradeLevel", classroom.getGradeLevel() != null ? classroom.getGradeLevel() : 0,
                    "subject", classroom.getSubject() != null ? classroom.getSubject() : "",
                    "status", classroom.getStatus() == null ? "ACTIVE" : classroom.getStatus().name(),
                    "writable", classroomService.isWritable(classroom)));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/classrooms/{id}/check-access/{userId}")
    public ResponseEntity<Map<String, Object>> checkAccess(@PathVariable Long id, @PathVariable Long userId) {
        try {
            Classroom classroom = classroomService.getActiveClassroom(id);
            boolean isTeacher = classroom.getTeacherId().equals(userId);
            
            boolean isMember = false;
            if (!isTeacher) {
                try {
                    classroomService.getRosterForMember(id, userId);
                    isMember = true;
                } catch (Exception ex) {
                    isMember = false;
                }
            }
            
            return ResponseEntity.ok(Map.of(
                    "hasAccess", isTeacher || isMember,
                    "isTeacher", isTeacher,
                    "isMember", isMember
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("hasAccess", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/classrooms/{id}/notification-recipients")
    public ResponseEntity<Map<String, Object>> getNotificationRecipients(@PathVariable Long id) {
        Classroom classroom = classroomService.getActiveClassroom(id);
        List<Long> studentIds = classroomService.getActiveStudentIds(id);
        return ResponseEntity.ok(Map.of(
                "teacherId", classroom.getTeacherId(),
                "studentIds", studentIds,
                "classroomName", classroom.getName()));
    }
}
