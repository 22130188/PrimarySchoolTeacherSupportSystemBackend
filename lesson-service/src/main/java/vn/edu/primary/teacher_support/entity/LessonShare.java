package vn.edu.primary.teacher_support.entity;

import jakarta.persistence.*;
import lombok.*;
import vn.edu.primary.teacher_support.entity.enums.SharePermission;

import java.time.LocalDateTime;

@Entity
@Table(name = "lesson_shares", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"draft_id", "shared_with_user_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LessonShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "draft_id", nullable = false)
    private Long draftId;

    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;

    @Column(name = "shared_with_user_id", nullable = false)
    private Long sharedWithUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SharePermission permission;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
