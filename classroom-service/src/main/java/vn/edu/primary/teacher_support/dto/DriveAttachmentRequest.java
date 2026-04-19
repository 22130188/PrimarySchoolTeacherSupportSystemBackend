package vn.edu.primary.teacher_support.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriveAttachmentRequest {
    private String fileId;
    private String driveUrl;
    private String title;
}
