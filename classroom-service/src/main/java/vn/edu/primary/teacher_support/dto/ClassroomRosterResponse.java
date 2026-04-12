package vn.edu.primary.teacher_support.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassroomRosterResponse {

    private Long classroomId;
    private String classroomName;
    private TeacherInfo teacher;
    private List<StudentMember> students;
    private List<InvitedStudent> invited;
    private List<WaitingRegister> waitingRegister;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TeacherInfo {
        private Long teacherId;
        private String name;
        private String email;
        private String avatarUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StudentMember {
        private Long memberId;
        private Long studentId;
        private String name;
        private String email;
        private String avatarUrl;
        private LocalDateTime joinedAt;
        private String joinType;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InvitedStudent {
        private Long invitationId;
        private String email;
        private Long studentId;
        private String studentName;
        private LocalDateTime invitedAt;
        private String status;
        private LocalDateTime expiredAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WaitingRegister {
        private Long invitationId;
        private String email;
        private LocalDateTime invitedAt;
        private String status;
    }
}
