package vn.edu.primary.translate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TranslateRequest {

    @NotBlank(message = "Text không được để trống")
    @Size(max = 10000, message = "Text không được vượt quá 10000 ký tự")
    private String text;

    @com.fasterxml.jackson.annotation.JsonProperty("source_lang")
    @Builder.Default
    private String sourceLang = "vi";

    @com.fasterxml.jackson.annotation.JsonProperty("target_lang")
    @Builder.Default
    private String targetLang = "en";
}
