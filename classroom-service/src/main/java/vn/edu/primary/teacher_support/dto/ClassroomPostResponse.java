package vn.edu.primary.teacher_support.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassroomPostResponse {

    private Long id;
    private Long classroomId;
    private Long authorId;
    private String authorName;
    private String authorAvatarUrl;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean canDelete;
    private List<AttachmentItem> attachments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AttachmentItem {
        private Long id;
        private String driveFileId;
        private String name;
        private String mimeType;
        private Long sizeBytes;
        private String iconLink;
        private String thumbnailLink;
        private String webViewLink;
        private String webContentLink;
    }
}
