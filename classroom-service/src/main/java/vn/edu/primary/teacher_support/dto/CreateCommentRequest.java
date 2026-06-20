package vn.edu.primary.teacher_support.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCommentRequest {

    @NotBlank(message = "Nội dung nhận xét không được để trống")
    @Size(max = 2000, message = "Nội dung nhận xét tối đa 2000 ký tự")
    private String content;
}
