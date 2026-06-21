package vn.edu.primary.teacher_support.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NotificationBroadcastRequest {
    @NotBlank
    private String targetRole;
    @NotBlank
    private String title;
    private String message;
    private String actionUrl;
}
