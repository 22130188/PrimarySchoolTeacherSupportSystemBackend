package vn.edu.primary.tts.dto;

import lombok.*;
import vn.edu.primary.tts.entity.AudioRecord;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AudioRecordResponse {

    private Long id;
    private String text;
    private String audioUrl;
    private Long userId;
    private String userName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AudioRecordResponse fromEntity(AudioRecord record) {
        return AudioRecordResponse.builder()
                .id(record.getId())
                .text(record.getText())
                .audioUrl(record.getAudioUrl())
                .userId(record.getUserId())
                .userName(record.getUserName())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}
