package vn.edu.primary.test.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateTestRequest {
    private String name;
    private String subject;
    private String grade;
    private Integer duration;
    private String description;
    private String lessonContentName;
    private String status;
    private String testType;
    private Long userId;
    private String userName;
    private Boolean includeAnswers;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private List<QuestionDTO> questions;
}
