package vn.edu.primary.teacher_support.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.primary.teacher_support.dto.ClassroomPostResponse;
import vn.edu.primary.teacher_support.dto.CreateClassroomPostRequest;
import vn.edu.primary.teacher_support.service.AuthHelper;
import vn.edu.primary.teacher_support.service.ClassroomPostService;

import java.util.List;

@RestController
@RequestMapping("/api/classrooms/{classroomId}/posts")
@RequiredArgsConstructor
public class ClassroomPostController {

    private final ClassroomPostService classroomPostService;
    private final AuthHelper authHelper;

    @GetMapping
    public ResponseEntity<List<ClassroomPostResponse>> getPosts(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long classroomId,
            @RequestParam(name = "limit", defaultValue = "30") int limit
    ) {
        Long userId = authHelper.extractUserId(authorization);
        return ResponseEntity.ok(classroomPostService.getPosts(classroomId, userId, limit));
    }

    @PostMapping
    public ResponseEntity<ClassroomPostResponse> createPost(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long classroomId,
            @Valid @RequestBody CreateClassroomPostRequest request
    ) {
        Long userId = authHelper.extractUserId(authorization);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(classroomPostService.createPost(classroomId, userId, request));
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<ClassroomPostResponse> updatePost(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long classroomId,
            @PathVariable Long postId,
            @Valid @RequestBody CreateClassroomPostRequest request
    ) {
        Long userId = authHelper.extractUserId(authorization);
        return ResponseEntity.ok(classroomPostService.updatePost(classroomId, postId, userId, request));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long classroomId,
            @PathVariable Long postId
    ) {
        Long userId = authHelper.extractUserId(authorization);
        classroomPostService.deletePost(classroomId, postId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<vn.edu.primary.teacher_support.dto.PostCommentResponse>> getComments(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long classroomId,
            @PathVariable Long postId
    ) {
        Long userId = authHelper.extractUserId(authorization);
        return ResponseEntity.ok(classroomPostService.getPostComments(classroomId, postId, userId));
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<vn.edu.primary.teacher_support.dto.PostCommentResponse> createComment(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long classroomId,
            @PathVariable Long postId,
            @Valid @RequestBody vn.edu.primary.teacher_support.dto.CreateCommentRequest request
    ) {
        Long userId = authHelper.extractUserId(authorization);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(classroomPostService.createPostComment(classroomId, postId, userId, request));
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long classroomId,
            @PathVariable Long postId,
            @PathVariable Long commentId
    ) {
        Long userId = authHelper.extractUserId(authorization);
        classroomPostService.deletePostComment(classroomId, postId, commentId, userId);
        return ResponseEntity.noContent().build();
    }
}
