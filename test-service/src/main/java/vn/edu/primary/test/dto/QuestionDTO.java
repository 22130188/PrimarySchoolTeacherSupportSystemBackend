package vn.edu.primary.test.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.edu.primary.test.entity.QuestionType;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class QuestionDTO {
    private Long id;
    
    private Object type;
    
    private String content;
    private Integer points;
    private String title;
    private Integer numberQuestions;
    
    @JsonProperty("answers")
    private List<AnswerDTO> answers;
    
    @JsonProperty("audioUrl")
    private String audioUrl;
    
    @JsonProperty("imageUrl")
    private String imageUrl;
    
    @JsonProperty("transcript")
    private String transcript;
    
    private Integer orderIndex;
    
    public QuestionType getQuestionType() {
        if (type == null) {
            return QuestionType.MULTIPLE_CHOICE;
        }
        if (type instanceof QuestionType) {
            return (QuestionType) type;
        }
        if (type instanceof String) {
            try {
                return QuestionType.valueOf((String) type);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid question type: {}, defaulting to MULTIPLE_CHOICE", type);
                return QuestionType.MULTIPLE_CHOICE;
            }
        }
        return QuestionType.MULTIPLE_CHOICE;
    }
}
