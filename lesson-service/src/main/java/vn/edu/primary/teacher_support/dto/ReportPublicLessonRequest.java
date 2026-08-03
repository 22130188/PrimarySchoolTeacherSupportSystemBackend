package vn.edu.primary.teacher_support.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ReportPublicLessonRequest {
    @NotBlank
    private String reason;
    private String detail;
}
