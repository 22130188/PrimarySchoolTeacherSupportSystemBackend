package vn.edu.primary.teacher_support.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.primary.teacher_support.dto.*;
import vn.edu.primary.teacher_support.service.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentClassroomController {

    private final MembershipService membershipService;
    private final InvitationService invitationService;
    private final ClassroomService classroomService;
    private final AuthHelper authHelper;

    @GetMapping("/classrooms/{id}")
    public ResponseEntity<ClassroomResponse> getClassroomDetail(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        authHelper.validateStudent(authorization);
        return ResponseEntity.ok(classroomService.getClassroom(id, null));
    }

    @GetMapping("/classrooms/{id}/roster")
    public ResponseEntity<ClassroomRosterResponse> getRoster(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        authHelper.validateStudent(authorization);
        Long userId = authHelper.extractUserId(authorization);
        return ResponseEntity.ok(classroomService.getRosterForMember(id, userId));
    }

    @GetMapping("/classrooms")
    public ResponseEntity<List<ClassroomResponse>> getMyClassrooms(
            @RequestHeader("Authorization") String authorization) {
        authHelper.validateStudent(authorization);
        Long userId = authHelper.extractUserId(authorization);
        return ResponseEntity.ok(membershipService.getMyJoinedClassrooms(userId));
    }

    @PostMapping("/classrooms/join/invite-link")
    public ResponseEntity<ClassroomResponse> joinByInviteLink(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody JoinByLinkRequest request) {
        authHelper.validateStudent(authorization);
        Long userId = authHelper.extractUserId(authorization);
        return ResponseEntity.ok(membershipService.joinByInviteLink(request.getToken(), userId));
    }

    @PostMapping("/classrooms/join/class-code")
    public ResponseEntity<ClassroomResponse> joinByClassCode(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody JoinByCodeRequest request) {
        authHelper.validateStudent(authorization);
        Long userId = authHelper.extractUserId(authorization);
        return ResponseEntity.ok(membershipService.joinByClassCode(request.getClassCode(), userId));
    }

    @PostMapping("/classrooms/join/invitation/{token}")
    public ResponseEntity<ClassroomResponse> joinByInvitationToken(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String token) {
        authHelper.validateStudent(authorization);
        Long userId = authHelper.extractUserId(authorization);
        String email = authHelper.extractEmail(authorization);
        return ResponseEntity.ok(membershipService.joinByInvitationToken(token, userId, email));
    }

    @GetMapping("/invitations")
    public ResponseEntity<List<InvitationResponse>> getMyInvitations(
            @RequestHeader("Authorization") String authorization) {
        authHelper.validateStudent(authorization);
        Long userId = authHelper.extractUserId(authorization);
        String email = authHelper.extractEmail(authorization);
        return ResponseEntity.ok(invitationService.getMyInvitations(userId, email));
    }

    @PostMapping("/invitations/{id}/accept")
    public ResponseEntity<ClassroomResponse> acceptInvitation(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        authHelper.validateStudent(authorization);
        Long userId = authHelper.extractUserId(authorization);
        String email = authHelper.extractEmail(authorization);
        return ResponseEntity.ok(membershipService.acceptInvitationFromSystem(id, userId, email));
    }

    @PostMapping("/invitations/{id}/reject")
    public ResponseEntity<Map<String, String>> rejectInvitation(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        authHelper.validateStudent(authorization);
        Long userId = authHelper.extractUserId(authorization);
        String email = authHelper.extractEmail(authorization);
        invitationService.rejectInvitation(id, userId, email);
        return ResponseEntity.ok(Map.of("message", "Đã từ chối lời mời"));
    }

    @DeleteMapping("/classrooms/{id}/leave")
    public ResponseEntity<Void> leaveClassroom(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        authHelper.validateStudent(authorization);
        Long userId = authHelper.extractUserId(authorization);
        membershipService.leaveClassroom(id, userId);
        return ResponseEntity.noContent().build();
    }
}
