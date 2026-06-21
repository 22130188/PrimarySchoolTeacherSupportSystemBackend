package vn.edu.primary.teacher_support.dto;

import lombok.Data;

import java.util.List;

@Data
public class NotificationCreateRequest {
    private Long targetUserId;
    private List<Long> targetUserIds;
    private Long actorUserId;
    private String actorName;
    private String type;
    private String title;
    private String message;
    private String actionUrl;
    private String resourceType;
    private String resourceId;
}
