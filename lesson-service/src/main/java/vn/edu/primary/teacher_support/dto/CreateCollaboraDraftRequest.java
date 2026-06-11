package vn.edu.primary.teacher_support.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateCollaboraDraftRequest {
    @NotBlank(message = "Tieu de khong duoc de trong")
    private String title;

    @NotBlank(message = "Mon hoc khong duoc de trong")
    private String subject;

    @NotBlank(message = "Lop khong duoc de trong")
    private String grade;

    @NotBlank(message = "Loai Collabora khong duoc de trong")
    private String type;
}
