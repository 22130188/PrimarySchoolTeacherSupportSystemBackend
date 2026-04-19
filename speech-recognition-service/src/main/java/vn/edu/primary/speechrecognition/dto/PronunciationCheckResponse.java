package vn.edu.primary.speechrecognition.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PronunciationCheckResponse {
    private Boolean success;
    private String message;
    
    @JsonProperty("recognized_text")
    private String recognizedText;
    
    @JsonProperty("accuracy_score")
    private String accuracyScore;
    
    private String feedback;
    private String error;
}
