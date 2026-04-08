package vn.edu.primary.teacher_support.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvitationResponse {

    private Long id;
    private Long classroomId;
    private String classroomName;
    private String email;
    private Long studentId;
    private String studentName;
    private String status;
    private String token;
    private LocalDateTime invitedAt;
    private LocalDateTime expiredAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime rejectedAt;
}
