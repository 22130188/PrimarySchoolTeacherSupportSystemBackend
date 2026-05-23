package vn.edu.primary.teacher_support.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassroomResponse {

    private Long id;
    private String name;
    private String description;
    private Long teacherId;
    private String teacherName;
    private String teacherEmail;
    private String teacherAvatarUrl;
    private String classCode;
    private String inviteLink;
    private int studentCount;
    private Integer gradeLevel;
    private String subject;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
