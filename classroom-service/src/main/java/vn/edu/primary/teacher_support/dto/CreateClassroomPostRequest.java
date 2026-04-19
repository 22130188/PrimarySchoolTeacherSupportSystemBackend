package vn.edu.primary.teacher_support.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateClassroomPostRequest {

    @Size(max = 5000, message = "Nội dung không được vượt quá 5000 ký tự")
    private String content;

    @Valid
    private List<DriveAttachmentRequest> attachments;
}
