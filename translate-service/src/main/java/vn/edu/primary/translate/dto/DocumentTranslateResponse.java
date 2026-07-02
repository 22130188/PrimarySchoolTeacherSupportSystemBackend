package vn.edu.primary.translate.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentTranslateResponse {

    private boolean success;
    private String message;

    @com.fasterxml.jackson.annotation.JsonProperty("original_text")
    private String originalText;

    @com.fasterxml.jackson.annotation.JsonProperty("translated_text")
    private String translatedText;
    
    private List<TranslatedSegment> segments;

    @com.fasterxml.jackson.annotation.JsonProperty("source_lang")
    private String sourceLang;

    @com.fasterxml.jackson.annotation.JsonProperty("target_lang")
    private String targetLang;

    @com.fasterxml.jackson.annotation.JsonProperty("total_segments")
    private int totalSegments;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TranslatedSegment {
        private String original;
        private String translated;
    }
}
