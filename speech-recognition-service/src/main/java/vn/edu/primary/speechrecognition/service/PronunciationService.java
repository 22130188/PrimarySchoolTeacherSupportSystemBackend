package vn.edu.primary.speechrecognition.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface PronunciationService {
    Map<String, Object> checkPronunciation(String targetText, MultipartFile audioFile) throws Exception;
    Map<String, Object> checkPronunciationWithVosk(String targetText, MultipartFile audioFile) throws Exception;
}
