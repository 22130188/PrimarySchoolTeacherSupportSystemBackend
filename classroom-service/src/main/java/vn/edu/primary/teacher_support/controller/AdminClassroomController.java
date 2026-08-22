package vn.edu.primary.teacher_support.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.primary.teacher_support.dto.*;
import vn.edu.primary.teacher_support.entity.enums.ClassroomStatus;
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
    public ResponseEntity<AdminClassroomPageResponse> getClassrooms(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(required = false) ClassroomStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        authHelper.validateAdmin(authorization);
        return ResponseEntity.ok(adminClassroomService.getClassrooms(
                page, size, status, keyword, sort, direction));
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

    @PostMapping("/{id}/lock")
    public ResponseEntity<AdminClassroomResponse> lockClassroom(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id,
            @Valid @RequestBody ClassroomStatusActionRequest request,
            HttpServletRequest httpRequest) {
        authHelper.validateAdmin(authorization);
        Long adminId = authHelper.extractUserId(authorization);
        return ResponseEntity.ok(adminClassroomService.lockClassroom(
                id, request.getReason().trim(), adminId, authorization, clientIp(httpRequest)));
    }

    @PostMapping("/{id}/unlock")
    public ResponseEntity<AdminClassroomResponse> unlockClassroom(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id,
            @Valid @RequestBody ClassroomStatusActionRequest request,
            HttpServletRequest httpRequest) {
        authHelper.validateAdmin(authorization);
        Long adminId = authHelper.extractUserId(authorization);
        return ResponseEntity.ok(adminClassroomService.unlockClassroom(
                id, request.getReason().trim(), adminId, authorization, clientIp(httpRequest)));
    }
    @GetMapping("/{id}/members")
    public ResponseEntity<ClassroomRosterResponse> getClassroomMembers(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        authHelper.validateAdmin(authorization);
        return ResponseEntity.ok(adminClassroomService.getClassroomMembers(id));
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }}
