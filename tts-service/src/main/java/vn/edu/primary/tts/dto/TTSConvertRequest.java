package vn.edu.primary.tts.dto;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TTSConvertRequest {

    @NotBlank(message = "Text không được để trống")
    @Size(min = 1, max = 5000, message = "Text phải từ 1 đến 5000 ký tự")
    private String text;
}
