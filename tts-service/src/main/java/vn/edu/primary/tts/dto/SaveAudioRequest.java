package vn.edu.primary.tts.dto;

import lombok.*;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveAudioRequest {

    @NotNull(message = "Text không được null")
    private String text;

    @NotNull(message = "Audio URL không được null")
    private String audioUrl;

    @NotNull(message = "User ID không được null")
    private Long userId;

    private String userName;
    private String audioName;
    private String subject;
}
