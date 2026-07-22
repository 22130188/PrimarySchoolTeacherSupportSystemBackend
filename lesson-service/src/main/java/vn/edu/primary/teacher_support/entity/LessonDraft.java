package vn.edu.primary.teacher_support.entity;

import jakarta.persistence.*;
import lombok.*;
import vn.edu.primary.teacher_support.entity.enums.LessonDraftStatus;
import vn.edu.primary.teacher_support.entity.enums.PublicVerificationStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "lesson_drafts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LessonDraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private String grade;

    @Column(length = 50)
    private String volume;

    @Column(length = 100)
    private String book;

    @Column(nullable = false)
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('DRAFT','PUBLISHED','ARCHIVED') DEFAULT 'DRAFT'")
    @Builder.Default
    private LessonDraftStatus status = LessonDraftStatus.DRAFT;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String canvasJson;

    /** Visible to all teachers when true (students blocked at API). */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isPublic = false;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private PublicVerificationStatus publicVerificationStatus = PublicVerificationStatus.UNVERIFIED;

    @Column
    private LocalDateTime publicPublishedAt;

    @Column(nullable = false)
    @Builder.Default
    private Integer publicCopyCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Double publicAverageRating = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private Integer publicRatingCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer publicOpenReportCount = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = LessonDraftStatus.DRAFT;
        if (isPublic == null) isPublic = false;
        if (publicVerificationStatus == null) publicVerificationStatus = PublicVerificationStatus.UNVERIFIED;
        if (publicCopyCount == null) publicCopyCount = 0;
        if (publicAverageRating == null) publicAverageRating = 0.0;
        if (publicRatingCount == null) publicRatingCount = 0;
        if (publicOpenReportCount == null) publicOpenReportCount = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
