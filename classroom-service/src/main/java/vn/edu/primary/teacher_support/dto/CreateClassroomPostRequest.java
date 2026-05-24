package vn.edu.primary.teacher_support.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import vn.edu.primary.teacher_support.entity.enums.PostType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateClassroomPostRequest {

    private PostType postType;
    private String title;
    private Integer attemptLimit;
    private Integer questionCount;
    private Integer maxPoints;
    private LocalDateTime startAt;
    private Integer durationMinutes;
    private Long referenceTestId;
    private String referenceTestName;

    @Size(max = 5000, message = "Nội dung không được vượt quá 5000 ký tự")
    private String content;

    @Valid
    private List<DriveAttachmentRequest> attachments;
}
