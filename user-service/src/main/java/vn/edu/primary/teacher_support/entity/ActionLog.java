package vn.edu.primary.teacher_support.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "action_logs", indexes = {
        @Index(name = "idx_action_logs_created_at", columnList = "created_at"),
        @Index(name = "idx_action_logs_user_id", columnList = "user_id"),
        @Index(name = "idx_action_logs_module", columnList = "module"),
        @Index(name = "idx_action_logs_action", columnList = "action"),
        @Index(name = "idx_action_logs_severity", columnList = "severity"),
        @Index(name = "idx_action_logs_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
public class ActionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(length = 100)
    private String username;

    @Column(name = "client_identifier", length = 255)
    private String clientIdentifier;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(nullable = false, length = 80)
    private String module;

    @Column(name = "resource_id", length = 150)
    private String resourceId;

    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod;

    @Column(nullable = false, length = 500)
    private String endpoint;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Severity severity;

    @Column(nullable = false, length = 10)
    private String status;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Severity { INFO, WARNING, DANGER, ALERT }
}
