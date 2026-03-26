package vn.edu.primary.teacher_support.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class UpdateClassesRequest {
    private List<TeacherClassDto> classes;

    @Data
    public static class TeacherClassDto {
        @NotBlank(message = "Lớp không được để trống")
        private String grade;

        @NotBlank(message = "Môn học không được để trống")
        private String subject;
    }
}