package vn.edu.primary.teacher_support.dto;

import lombok.*;
import vn.edu.primary.teacher_support.entity.enums.LessonTemplateStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonTemplateResponse {
    private Long id;
    private String title;
    private String description;
    private String subject;
    private String grade;
    private String type;
    private String fileName;
    private String extension;
    private LessonTemplateStatus status;
    private Long createdByUserId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
