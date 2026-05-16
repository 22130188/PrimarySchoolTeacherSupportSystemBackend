package vn.edu.primary.test.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

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
    private String status;
    private Long userId;
    private String userName;
    private Boolean includeAnswers;
    private List<QuestionDTO> questions;
}
