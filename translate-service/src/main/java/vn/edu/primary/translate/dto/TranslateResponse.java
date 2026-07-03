package vn.edu.primary.translate.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TranslateResponse {

    private boolean success;
    private String message;

    @com.fasterxml.jackson.annotation.JsonProperty("original_text")
    private String originalText;

    @com.fasterxml.jackson.annotation.JsonProperty("translated_text")
    private String translatedText;

    @com.fasterxml.jackson.annotation.JsonProperty("source_lang")
    private String sourceLang;

    @com.fasterxml.jackson.annotation.JsonProperty("target_lang")
    private String targetLang;
}
