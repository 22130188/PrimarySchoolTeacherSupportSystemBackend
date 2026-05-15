package vn.edu.primary.teacher_support.dto;

import lombok.*;
import vn.edu.primary.teacher_support.entity.enums.LessonDraftStatus;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DraftResponse {
    private Long id;
    private String title;
    private String subject;
    private String grade;
    private String type;
    private LessonDraftStatus status;
    private String canvasJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
