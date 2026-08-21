package vn.edu.primary.translate.service.impl;

import vn.edu.primary.translate.service.TranslateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
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
    public Map<String, Object> extractTextFromFile(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        try {
            String endpoint = pythonApiUrl + "/api/translate/extract";

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", file.getResource());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

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

    @Override
    public FileTranslateResult translateDocumentFile(MultipartFile file, String sourceLang, String targetLang) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        String base = pythonApiUrl == null ? "http://python-api:8001" : pythonApiUrl.replaceAll("/$", "");
        String endpoint = base + "/api/translate/document/file";

        try {
            byte[] bytes = file.getBytes();
            String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document";
            ByteArrayResource resource = new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return originalName;
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", resource);
            body.add("source_lang", sourceLang != null ? sourceLang : "vi");
            body.add("target_lang", targetLang != null ? targetLang : "en");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setAccept(List.of(MediaType.ALL));

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    byte[].class
            );

            if (response.getBody() == null || response.getBody().length == 0) {
                throw new Exception("Empty response from Python document/file API");
            }

            MediaType contentType = response.getHeaders().getContentType();
            String ct = contentType != null ? contentType.toString() : "application/octet-stream";
            String filename = "translated_" + originalName;
            ContentDisposition disposition = response.getHeaders().getContentDisposition();
            if (disposition.getFilename() != null && !disposition.getFilename().isBlank()) {
                filename = disposition.getFilename();
            }

            return new FileTranslateResult(response.getBody(), ct, filename);
        } catch (Exception e) {
            throw new Exception("Error calling Python document/file service: " + e.getMessage(), e);
        }
    }
}
