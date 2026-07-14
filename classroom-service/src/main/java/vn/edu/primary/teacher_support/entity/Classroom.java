package vn.edu.primary.teacher_support.entity;

import jakarta.persistence.*;
import vn.edu.primary.teacher_support.entity.enums.ClassroomStatus;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "classrooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Classroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "class_code", nullable = false, unique = true, length = 8)
    private String classCode;

    @Column(name = "invite_link_token", nullable = false, unique = true, length = 64)
    private String inviteLinkToken;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'")
    @Builder.Default
    private ClassroomStatus status = ClassroomStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_before_lock", length = 20)
    private ClassroomStatus statusBeforeLock;

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "grade_level")
    private Integer gradeLevel;

    @Column(name = "subject", length = 50)
    private String subject;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
