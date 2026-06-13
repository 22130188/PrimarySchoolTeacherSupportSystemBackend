package vn.edu.primary.test.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonContentDto {
    private Long id;
    private String subject;
    private String grade;
    private String name;
    private String description;
    private Boolean isActive;
    private Long createdByUserId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
