package vn.edu.primary.teacher_support.entity;

import jakarta.persistence.*;
import lombok.*;
import vn.edu.primary.teacher_support.entity.enums.LessonDraftStatus;

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

    @Column(nullable = false)
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('DRAFT','PUBLISHED','ARCHIVED') DEFAULT 'DRAFT'")
    @Builder.Default
    private LessonDraftStatus status = LessonDraftStatus.DRAFT;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String canvasJson;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = LessonDraftStatus.DRAFT;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
