package vn.edu.primary.teacher_support.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminClassroomResponse {

    private Long id;
    private String name;
    private String description;
    private Long teacherId;
    private String teacherName;
    private String teacherEmail;
    private String classCode;
    private String inviteLink;
    private int studentCount;
    private int pendingInvitationCount;
    private Integer gradeLevel;
    private String classGroup;
    private Long classCategoryId;
    private Long groupCategoryId;
    private String classDisplayName;
    private String subject;
    private Long createdBy;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private String status;
    private String statusBeforeLock;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
