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
}
