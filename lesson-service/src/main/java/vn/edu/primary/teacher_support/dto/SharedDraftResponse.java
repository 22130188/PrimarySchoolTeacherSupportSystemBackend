package vn.edu.primary.teacher_support.dto;

import lombok.*;
import vn.edu.primary.teacher_support.entity.enums.LessonDraftStatus;
import vn.edu.primary.teacher_support.entity.enums.SharePermission;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SharedDraftResponse {
    private Long id;
    private String title;
    private String subject;
    private String grade;
    private String volume;
    private String book;
    private String type;
    private LessonDraftStatus status;
    private String canvasJson;
    private SharePermission permission;
    private String ownerName;
    private String ownerEmail;
    private String ownerAvatarUrl;
    private long commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
