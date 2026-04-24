package vn.edu.primary.teacher_support.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DraftResponse {
    private Long id;
    private String title;
    private String type;
    private String canvasJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
