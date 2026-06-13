package vn.edu.primary.teacher_support.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDto {

    private Long id;
    private String type;
    private String name;
    private String code;
    private String description;
    private String grade;
    private String subject;
    private Boolean isActive;
    private String createdByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
