package vn.edu.primary.teacher_support.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SaveDraftRequest {

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    @NotBlank(message = "Loại bài giảng không được để trống")
    private String type;

    @NotBlank(message = "Dữ liệu canvas không được để trống")
    private String canvasJson;

    private Long draftId;
}
