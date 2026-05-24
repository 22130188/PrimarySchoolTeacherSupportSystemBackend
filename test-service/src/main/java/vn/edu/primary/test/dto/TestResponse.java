package vn.edu.primary.test.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.primary.test.entity.TestStatus;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TestResponse {
    private Long id;
    private String name;
    private String subject;
    private String grade;
    private Integer duration;
    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String docxFileUrl;
    private String description;
    private Integer totalPoints;
    private Integer questionCount;
    private String testType;
    private TestStatus status;
    private String lessonContentName;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private List<QuestionDTO> questions;
}
