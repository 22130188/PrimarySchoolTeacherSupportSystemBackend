package vn.edu.primary.teacher_support.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RatePublicLessonRequest {
    @NotNull
    @Min(1)
    @Max(5)
    private Integer stars;
}
