package vn.edu.primary.teacher_support.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import vn.edu.primary.teacher_support.entity.enums.SharePermission;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ShareLessonRequest {

    @NotBlank(message = "Email không được để trống")
    private String email;

    @NotNull(message = "Quyền chia sẻ không được để trống")
    private SharePermission permission;
}
