package vn.edu.primary.teacher_support.entity;

import jakarta.persistence.*;
import lombok.*;
import vn.edu.primary.teacher_support.entity.enums.PostType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "classroom_posts", indexes = {
        @Index(name = "idx_post_classroom_created", columnList = "classroom_id,created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class ClassroomPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false)
    @Builder.Default
    private PostType postType = PostType.ANNOUNCEMENT;

    @Column(name = "title", length = 500)
    private String title;

    @Column(name = "attempt_limit")
    private Integer attemptLimit;

    @Column(name = "question_count")
    private Integer questionCount;

    @Column(name = "max_points")
    private Integer maxPoints;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "reference_test_id")
    private Long referenceTestId;

    @Column(name = "reference_test_name", length = 255)
    private String referenceTestName;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ClassroomPostAttachment> attachments = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
