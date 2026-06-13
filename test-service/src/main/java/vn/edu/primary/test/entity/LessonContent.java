package vn.edu.primary.test.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lesson_contents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String subject;

    @Column(nullable = false, length = 50)
    private String grade;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 500)
    private String description;

    private Boolean isActive = true;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
