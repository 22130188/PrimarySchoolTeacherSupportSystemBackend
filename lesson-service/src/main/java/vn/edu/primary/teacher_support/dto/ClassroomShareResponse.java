package vn.edu.primary.teacher_support.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClassroomShareResponse {
    private Long id;
    private Long draftId;
    private Long classroomId;
    private String classroomName;
    private LocalDateTime createdAt;
}
