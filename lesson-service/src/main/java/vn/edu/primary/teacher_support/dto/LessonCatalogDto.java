package vn.edu.primary.teacher_support.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonCatalogDto {
    private Long id;
    private String subject;
    private String grade;
    private String volume;
    private String book;
    private String name;
    private String description;
    private Boolean isActive;
    private Long createdByUserId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}