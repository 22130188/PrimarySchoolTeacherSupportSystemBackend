package vn.edu.primary.teacher_support.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClassroomStatusActionRequest {
    @NotBlank(message = "Vui lòng nhập lý do")
    @Size(max = 1000, message = "Lý do không được vượt quá 1000 ký tự")
    private String reason;
}
