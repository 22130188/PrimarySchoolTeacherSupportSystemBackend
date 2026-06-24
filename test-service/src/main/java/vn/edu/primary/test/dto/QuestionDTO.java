package vn.edu.primary.test.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.edu.primary.test.entity.QuestionType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
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
    
    @JsonProperty("lessonContentName")
    private String lessonContentName;
    
    @JsonProperty("subject")
    private String subject;
    
    @JsonProperty("createdByName")
    private String createdByName;
    
    @JsonProperty("createdBy")
    private Long createdBy;
    
    @JsonProperty("answers")
    private List<AnswerDTO> answers;
    
    @JsonProperty("audioUrl")
    private String audioUrl;
    
    @JsonProperty("imageUrl")
    private String imageUrl;
    
    @JsonProperty("transcript")
    private String transcript;
    
    @JsonProperty("testType")
    private String testType;

    private Integer orderIndex;
    
    @JsonProperty("matchingPairs")
    private List<MatchingPairDTO> matchingPairs;
    
    @JsonProperty("textWithBlanks")
    private String textWithBlanks;
    
    @JsonProperty("blanks")
    private List<BlankDTO> blanks;
    
    @JsonProperty("prompt")
    private String prompt;
    
    @JsonProperty("maxLength")
    private Integer maxLength;
    
    @JsonProperty("rubric")
    private String rubric;

    @JsonProperty("isShared")
    private Boolean isShared;

    @JsonProperty(value = "createdAt", access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createdAt;

    @JsonProperty(value = "updatedAt", access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime updatedAt;
    
    @JsonIgnore
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
