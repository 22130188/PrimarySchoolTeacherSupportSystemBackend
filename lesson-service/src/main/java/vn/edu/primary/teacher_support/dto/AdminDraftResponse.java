package vn.edu.primary.teacher_support.dto;

import lombok.*;
import vn.edu.primary.teacher_support.entity.enums.LessonDraftStatus;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminDraftResponse {
    private Long id;
    private String title;
    private String subject;
    private String grade;
    private String volume;
    private String book;
    private String type;
    private LessonDraftStatus status;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
