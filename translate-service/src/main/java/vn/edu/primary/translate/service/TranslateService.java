package vn.edu.primary.translate.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

public interface TranslateService {

    Map<String, Object> translateText(String text, String sourceLang, String targetLang) throws Exception;


    Map<String, Object> translateDocument(String text, String sourceLang, String targetLang) throws Exception;

    Map<String, Object> extractTextFromFile(MultipartFile file) throws Exception;

    /** Translate uploaded file via Python; returns raw file bytes + content type + download name. */
    FileTranslateResult translateDocumentFile(MultipartFile file, String sourceLang, String targetLang) throws Exception;

    record FileTranslateResult(byte[] body, String contentType, String filename) {}
}
