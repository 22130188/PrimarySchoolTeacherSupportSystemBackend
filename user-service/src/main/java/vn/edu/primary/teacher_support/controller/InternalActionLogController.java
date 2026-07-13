package vn.edu.primary.teacher_support.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.primary.teacher_support.dto.ActionLogCreateRequest;
import vn.edu.primary.teacher_support.service.ActionLogService;

@RestController
@RequestMapping("/api/internal/action-logs")
@RequiredArgsConstructor
public class InternalActionLogController {
    private final ActionLogService actionLogService;

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody ActionLogCreateRequest request) {
        actionLogService.createAsync(request);
        return ResponseEntity.accepted().build();
    }
}
