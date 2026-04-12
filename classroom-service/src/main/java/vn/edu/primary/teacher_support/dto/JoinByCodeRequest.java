package vn.edu.primary.teacher_support.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinByCodeRequest {

    @NotBlank(message = "Class code không được để trống")
    private String classCode;
}
