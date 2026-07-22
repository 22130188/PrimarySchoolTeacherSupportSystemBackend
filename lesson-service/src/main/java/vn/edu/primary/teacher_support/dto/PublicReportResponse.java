package vn.edu.primary.teacher_support.dto;

import lombok.*;
import vn.edu.primary.teacher_support.entity.enums.PublicReportStatus;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PublicReportResponse {
    private Long id;
    private Long draftId;
    private String lessonTitle;
    /** Type of the reported lesson (DOCX, PPTX, COLLABORA_DOCX, COLLABORA_PPTX) — used by admin to open correct editor. */
    private String lessonType;
    private String ownerName;
    private Long reporterId;
    private String reporterName;
    private String reporterEmail;
    private String reason;
    private String detail;
    private PublicReportStatus status;
    private String adminNote;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}
