package vn.edu.primary.teacher_support.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TranslateCollaboraDraftRequest {
    private String sourceLang;

    private String targetLang;

    private String source_lang;

    private String target_lang;

    private String title;

    public String getEffectiveSourceLang() {
        return sourceLang != null && !sourceLang.isBlank() ? sourceLang : source_lang;
    }

    public String getEffectiveTargetLang() {
        return targetLang != null && !targetLang.isBlank() ? targetLang : target_lang;
    }
}