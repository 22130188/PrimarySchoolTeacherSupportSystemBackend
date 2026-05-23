package vn.edu.primary.teacher_support.dto;

import lombok.*;
import vn.edu.primary.teacher_support.entity.enums.SharePermission;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ShareResponse {
    private Long id;
    private Long draftId;
    private Long sharedWithUserId;
    private String sharedWithEmail;
    private String sharedWithName;
    private SharePermission permission;
    private LocalDateTime createdAt;
}
