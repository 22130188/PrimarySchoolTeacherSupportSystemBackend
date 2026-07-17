package vn.edu.primary.speechrecognition.service.impl;

import vn.edu.primary.speechrecognition.client.PythonTTSClient;
import vn.edu.primary.speechrecognition.service.PronunciationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@Service
public class PronunciationServiceImpl implements PronunciationService {

    @Autowired
    private PythonTTSClient pythonTTSClient;

    @Override
    public Map<String, Object> checkPronunciation(String targetText, MultipartFile audioFile) throws Exception {
        validateInput(targetText, audioFile);

        try {
            Map<String, Object> result = pythonTTSClient.checkPronunciation(targetText, audioFile);
            return requireResult(result);
        } catch (Exception e) {
            throw new Exception("Error calling Python pronunciation service: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> checkPronunciationWithVosk(String targetText, MultipartFile audioFile) throws Exception {
        validateInput(targetText, audioFile);

        try {
            Map<String, Object> result = pythonTTSClient.checkPronunciationWithVosk(targetText, audioFile);
            return requireResult(result);
        } catch (Exception e) {
            throw new Exception("Error calling Python Vosk pronunciation service: " + e.getMessage(), e);
        }
    }

    private void validateInput(String targetText, MultipartFile audioFile) {
        if (targetText == null || targetText.trim().isEmpty()) {
            throw new IllegalArgumentException("Target text cannot be empty");
        }

        if (audioFile == null || audioFile.isEmpty()) {
            throw new IllegalArgumentException("Audio file cannot be empty");
        }
    }

    private Map<String, Object> requireResult(Map<String, Object> result) throws Exception {
        if (result == null) {
            throw new Exception("Failed to check pronunciation from Python API");
        }
        return result;
    }
}
