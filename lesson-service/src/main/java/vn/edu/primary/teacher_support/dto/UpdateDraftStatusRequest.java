package vn.edu.primary.teacher_support.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import vn.edu.primary.teacher_support.entity.enums.LessonDraftStatus;

@Getter
@Setter
public class UpdateDraftStatusRequest {
    @NotNull(message = "Trạng thái không được để trống")
    private LessonDraftStatus status;
}
