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
    private String audioName;
    private String subject;
    private String grade;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AudioRecordResponse fromEntity(AudioRecord record) {
        return AudioRecordResponse.builder()
                .id(record.getId())
                .text(record.getText())
                .audioUrl(record.getAudioUrl())
                .userId(record.getUserId())
                .userName(record.getUserName())
                .audioName(record.getAudioName())
                .subject(record.getSubject())
                .grade(record.getGrade())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}
