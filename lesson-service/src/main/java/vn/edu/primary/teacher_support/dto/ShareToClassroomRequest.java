package vn.edu.primary.teacher_support.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ShareToClassroomRequest {
    @NotNull(message = "classroomId is required")
    private Long classroomId;
}
