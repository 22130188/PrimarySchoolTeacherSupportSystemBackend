package vn.edu.primary.teacher_support.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActionLogCreateRequest {
    private String authToken;
    private String username;
    private String clientIdentifier;
    private String action;
    private String module;
    private String resourceId;
    private String httpMethod;
    private String endpoint;
    private String severity;
    private String status;
    private String description;
    private String ipAddress;
}
