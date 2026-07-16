package vn.edu.primary.speechrecognition.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.primary.speechrecognition.config.FeignConfig;

import java.util.Map;

@FeignClient(name = "python-tts-api", url = "${python.api.url:http://localhost:8001}", configuration = FeignConfig.class)
public interface PythonTTSClient {

    @PostMapping(value = "/api/pronunciation/check", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Map<String, Object> checkPronunciation(
            @RequestPart("target_text") String targetText,
            @RequestPart("audio_file") MultipartFile audioFile
    );

    @PostMapping(value = "/api/pronunciation/check-vosk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Map<String, Object> checkPronunciationWithVosk(
            @RequestPart("target_text") String targetText,
            @RequestPart("audio_file") MultipartFile audioFile
    );
}
