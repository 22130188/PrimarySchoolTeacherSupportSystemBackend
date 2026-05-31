package vn.edu.primary.test.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttemptHistoryResponseDTO {
    private AttemptStatisticsDTO statistics;
    
    private List<Map<String, Object>> attempts;
    
    private String message;
    
    private Boolean isAvailable;
    
    private Object startAt;
}
