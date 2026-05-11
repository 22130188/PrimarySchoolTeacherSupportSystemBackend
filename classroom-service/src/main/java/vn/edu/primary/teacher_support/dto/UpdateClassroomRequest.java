package vn.edu.primary.teacher_support.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateClassroomRequest {

    @NotBlank(message = "Tên lớp không được để trống")
    private String name;

    private String description;

    private Integer gradeLevel;

    private String subject;
}
