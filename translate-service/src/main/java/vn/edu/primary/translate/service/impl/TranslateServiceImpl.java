package vn.edu.primary.translate.service.impl;

import vn.edu.primary.translate.service.TranslateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class TranslateServiceImpl implements TranslateService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${python.api.url}")
    private String pythonApiUrl;

    @Override
    public Map<String, Object> translateText(String text, String sourceLang, String targetLang) throws Exception {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text cannot be empty");
        }

        try {
            String endpoint = pythonApiUrl + "/api/translate/translate";

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("text", text);
            requestBody.put("source_lang", sourceLang);
            requestBody.put("target_lang", targetLang);

            @SuppressWarnings("unchecked")
            Map<String, Object> result = restTemplate.postForObject(endpoint, requestBody, Map.class);

            if (result == null) {
                throw new Exception("Failed to get translation from Python API");
            }

            return result;
        } catch (Exception e) {
            throw new Exception("Error calling Python translation service: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> translateDocument(String text, String sourceLang, String targetLang) throws Exception {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text cannot be empty");
        }

        try {
            String endpoint = pythonApiUrl + "/api/translate/document";

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("text", text);
            requestBody.put("source_lang", sourceLang);
            requestBody.put("target_lang", targetLang);

            @SuppressWarnings("unchecked")
            Map<String, Object> result = restTemplate.postForObject(endpoint, requestBody, Map.class);

            if (result == null) {
                throw new Exception("Failed to get document translation from Python API");
            }

            return result;
        } catch (Exception e) {
            throw new Exception("Error calling Python translation service: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> extractTextFromFile(org.springframework.web.multipart.MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        try {
            String endpoint = pythonApiUrl + "/api/translate/extract";

            org.springframework.util.MultiValueMap<String, Object> body = new org.springframework.util.LinkedMultiValueMap<>();
            body.add("file", file.getResource());

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);

            org.springframework.http.HttpEntity<org.springframework.util.MultiValueMap<String, Object>> requestEntity = new org.springframework.http.HttpEntity<>(body, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> result = restTemplate.postForObject(endpoint, requestEntity, Map.class);

            if (result == null) {
                throw new Exception("Failed to get extraction result from Python API");
            }

            return result;
        } catch (Exception e) {
            throw new Exception("Error calling Python file extract service: " + e.getMessage(), e);
        }
    }
}
