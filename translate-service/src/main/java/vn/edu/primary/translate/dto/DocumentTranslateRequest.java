package vn.edu.primary.translate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentTranslateRequest {

    @NotBlank(message = "Text không được để trống")
    @Size(max = 50000, message = "Tài liệu không được vượt quá 50000 ký tự")
    private String text;

    @com.fasterxml.jackson.annotation.JsonProperty("source_lang")
    @Builder.Default
    private String sourceLang = "vi";

    @com.fasterxml.jackson.annotation.JsonProperty("target_lang")
    @Builder.Default
    private String targetLang = "en";
}
