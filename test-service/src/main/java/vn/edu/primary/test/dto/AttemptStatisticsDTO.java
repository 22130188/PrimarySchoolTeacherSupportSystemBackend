package vn.edu.primary.test.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttemptStatisticsDTO {
    private Integer totalAttempts;
    
    private Integer completedAttempts;
    
    private Double averageScore;
    
    private Double averageScorePercentage;
    
    private Integer maxScore;
    
    private Integer minScore;
    
    private Double completionRate;
}
