package vn.edu.primary.teacher_support.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateDraftMetadataRequest {

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    @NotBlank(message = "Môn học không được để trống")
    private String subject;

    @NotBlank(message = "Lớp không được để trống")
    private String grade;

    private String volume;

    private String book;
}
