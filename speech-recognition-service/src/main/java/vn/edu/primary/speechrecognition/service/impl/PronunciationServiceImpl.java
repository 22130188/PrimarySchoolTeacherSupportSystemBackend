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
        if (targetText == null || targetText.trim().isEmpty()) {
            throw new IllegalArgumentException("Target text cannot be empty");
        }

        if (audioFile == null || audioFile.isEmpty()) {
            throw new IllegalArgumentException("Audio file cannot be empty");
        }

        try {
            Map<String, Object> result = pythonTTSClient.checkPronunciation(targetText, audioFile);
            if (result == null) {
                throw new Exception("Failed to check pronunciation from Python API");
            }
            return result;
        } catch (Exception e) {
            throw new Exception("Error calling Python pronunciation service: " + e.getMessage(), e);
        }
    }
}
