package vn.edu.primary.teacher_support.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_feedback", indexes = {
        @Index(name = "idx_feedback_status_created", columnList = "status,created_at"),
        @Index(name = "idx_feedback_user_created", columnList = "user_id,created_at")
})
@Getter @Setter @NoArgsConstructor
public class Feedback {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "user_id", nullable = false) private Long userId;
    @Column(name = "user_name", nullable = false, length = 150) private String userName;
    @Column(name = "user_email", length = 150) private String userEmail;
    @Column(nullable = false, length = 30) private String type;
    @Column(nullable = false, length = 180) private String title;
    @Column(nullable = false, columnDefinition = "TEXT") private String description;
    @Column(name = "page_url", length = 1000) private String pageUrl;
    @Column(name = "browser_info", length = 1000) private String browserInfo;
    @Column(nullable = false, length = 30) private String status = "NEW";
    @Column(name = "admin_reply", columnDefinition = "TEXT") private String adminReply;
    @Column(name = "replied_by") private Long repliedBy;
    @Column(name = "replied_at") private LocalDateTime repliedAt;
    @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt = LocalDateTime.now();
    @PreUpdate void touch() { updatedAt = LocalDateTime.now(); }
}
