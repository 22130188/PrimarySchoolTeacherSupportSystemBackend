package vn.edu.primary.teacher_support.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lesson_public_verification_config")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LessonPublicVerificationConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Builder.Default
    private Integer minCopyCount = 5;

    @Column(nullable = false)
    @Builder.Default
    private Double minAverageRating = 4.0;

    @Column(nullable = false)
    @Builder.Default
    private Integer minRatingCount = 3;

    /** Max open reports allowed while still eligible for VERIFIED (default 0). */
    @Column(nullable = false)
    @Builder.Default
    private Integer maxOpenReports = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer minPublicDays = 3;

    /** Auto-hide public when open reports reach this threshold. */
    @Column(nullable = false)
    @Builder.Default
    private Integer autoHideOpenReportThreshold = 3;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void touch() {
        updatedAt = LocalDateTime.now();
    }
}
