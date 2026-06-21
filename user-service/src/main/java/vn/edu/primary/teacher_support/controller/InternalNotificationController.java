package vn.edu.primary.teacher_support.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.primary.teacher_support.dto.NotificationCreateRequest;
import vn.edu.primary.teacher_support.service.NotificationService;

import java.util.Map;

@RestController
@RequestMapping("/api/internal/users/notifications")
@RequiredArgsConstructor
public class InternalNotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<Map<String, Integer>> create(@RequestBody NotificationCreateRequest request) {
        return ResponseEntity.ok(Map.of("created", notificationService.create(request)));
    }
}
