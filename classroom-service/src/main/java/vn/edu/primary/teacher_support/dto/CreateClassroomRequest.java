package vn.edu.primary.teacher_support.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateClassroomRequest {

    @NotBlank(message = "Tên lớp không được để trống")
    private String name;

    private String description;

    private Integer gradeLevel;

    private String classGroup;

    private Long classCategoryId;

    private Long groupCategoryId;

    private String classDisplayName;

    private String subject;
}
