package vn.edu.primary.teacher_support.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "access_logs", indexes = {
        @Index(name = "idx_access_logs_created_at", columnList = "created_at"),
        @Index(name = "idx_access_logs_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
public class AccessLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 150)
    private String username;

    @Column(length = 30)
    private String role;

    @Column(nullable = false, length = 40)
    private String action;

    @Column(length = 64)
    private String ip;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
