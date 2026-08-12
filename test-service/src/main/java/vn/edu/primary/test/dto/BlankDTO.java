package vn.edu.primary.test.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlankDTO {
    // The web editor uses Date.now() for newly added blanks, which exceeds
    // Integer.MAX_VALUE. Keep this as Long so the request can be deserialized.
    private Long id;
    private Integer position;
    private String correctAnswer;
    private Integer points;
}