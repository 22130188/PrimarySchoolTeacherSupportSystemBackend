package vn.edu.primary.teacher_support.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.primary.teacher_support.dto.*;
import vn.edu.primary.teacher_support.service.AdminClassroomService;
import vn.edu.primary.teacher_support.service.AuthHelper;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/classrooms")
@RequiredArgsConstructor
public class AdminClassroomController {

    private final AdminClassroomService adminClassroomService;
    private final AuthHelper authHelper;

    @GetMapping
    public ResponseEntity<List<AdminClassroomResponse>> getAllClassrooms(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(value = "includeDeleted", defaultValue = "false") boolean includeDeleted) {
        authHelper.validateAdmin(authorization);
        return ResponseEntity.ok(adminClassroomService.getAllClassrooms(includeDeleted));
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminDashboardStats> getDashboardStats(
            @RequestHeader("Authorization") String authorization) {
        authHelper.validateAdmin(authorization);
        return ResponseEntity.ok(adminClassroomService.getDashboardStats());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminClassroomResponse> getClassroomDetail(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        authHelper.validateAdmin(authorization);
        return ResponseEntity.ok(adminClassroomService.getClassroomDetail(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminClassroomResponse> updateClassroom(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id,
            @Valid @RequestBody UpdateClassroomRequest request) {
        authHelper.validateAdmin(authorization);
        return ResponseEntity.ok(adminClassroomService.updateClassroom(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteClassroom(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        authHelper.validateAdmin(authorization);
        adminClassroomService.softDeleteClassroom(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<AdminClassroomResponse> restoreClassroom(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        authHelper.validateAdmin(authorization);
        return ResponseEntity.ok(adminClassroomService.restoreClassroom(id));
    }

    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Map<String, String>> hardDeleteClassroom(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        authHelper.validateAdmin(authorization);
        adminClassroomService.hardDeleteClassroom(id);
        return ResponseEntity.ok(Map.of("message", "Đã xóa vĩnh viễn lớp học"));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<ClassroomRosterResponse> getClassroomMembers(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        authHelper.validateAdmin(authorization);
        return ResponseEntity.ok(adminClassroomService.getClassroomMembers(id));
    }

    @DeleteMapping("/{id}/members/{memberId}")
    public ResponseEntity<Void> removeMember(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id,
            @PathVariable Long memberId) {
        authHelper.validateAdmin(authorization);
        adminClassroomService.removeMember(id, memberId);
        return ResponseEntity.noContent().build();
    }
}
}
