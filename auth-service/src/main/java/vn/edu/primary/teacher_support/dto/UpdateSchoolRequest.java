package vn.edu.primary.teacher_support.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateSchoolRequest {
    @NotBlank(message = "Tên trường không được để trống")
    private String schoolName;
}