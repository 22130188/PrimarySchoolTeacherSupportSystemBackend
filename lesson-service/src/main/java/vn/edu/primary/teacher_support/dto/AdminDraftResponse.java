package vn.edu.primary.teacher_support.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminDraftResponse {
    private Long id;
    private String title;
    private String subject;
    private String grade;
    private String type;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
