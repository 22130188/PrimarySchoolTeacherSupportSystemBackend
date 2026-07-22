package vn.edu.primary.teacher_support.dto;

import lombok.*;
import vn.edu.primary.teacher_support.entity.enums.LessonDraftStatus;
import vn.edu.primary.teacher_support.entity.enums.PublicVerificationStatus;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PublicLessonResponse {
    private Long id;
    private String title;
    private String subject;
    private String grade;
    private String volume;
    private String book;
    private String type;
    private LessonDraftStatus status;
    private String canvasJson;

    private Long ownerId;
    private String ownerName;
    private String ownerEmail;
    private Boolean isOwner;

    private Boolean isPublic;
    private PublicVerificationStatus publicVerificationStatus;
    private LocalDateTime publicPublishedAt;
    private Integer publicCopyCount;
    private Double publicAverageRating;
    private Integer publicRatingCount;
    private Integer publicOpenReportCount;
    private Integer myRating;

    /** Populated by admin reevaluate: human-readable missing rules when still UNVERIFIED. */
    private java.util.List<String> missingConditions;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
