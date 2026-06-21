package vn.edu.primary.teacher_support.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private Long actorUserId;
    private String actorName;
    private String type;
    private String title;
    private String message;
    private String actionUrl;
    private String resourceType;
    private String resourceId;
    private boolean read;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
