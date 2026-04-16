package vn.edu.primary.tts.service.impl;

import vn.edu.primary.tts.config.CloudinaryConfig;
import vn.edu.primary.tts.dto.TTSConvertRequest;
import vn.edu.primary.tts.dto.SaveAudioRequest;
import vn.edu.primary.tts.dto.AudioRecordResponse;
import vn.edu.primary.tts.entity.AudioRecord;
import vn.edu.primary.tts.repository.AudioRecordRepository;
import vn.edu.primary.tts.service.TTSService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TTSServiceImpl implements TTSService {

    @Autowired
    private AudioRecordRepository audioRecordRepository;

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private CloudinaryConfig cloudinaryConfig;

    @Value("${python.tts.api-url}")
    private String pythonTtsApiUrl;

    private final RestTemplate restTemplate;

    public TTSServiceImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public String convertTextToSpeech(TTSConvertRequest request) throws Exception {
        String pythonEndpoint = pythonTtsApiUrl;
        if (pythonEndpoint.endsWith("/")) {
            pythonEndpoint = pythonEndpoint.substring(0, pythonEndpoint.length() - 1);
        }
        if (pythonEndpoint.endsWith("/tts")) {
            pythonEndpoint += "/convert";
        } else {
            pythonEndpoint += "/tts/convert";
        }

        Map<String, Object> responseFromPython = restTemplate.postForObject(
            pythonEndpoint,
            request,
            Map.class
        );

        if (responseFromPython == null || !responseFromPython.containsKey("filename")) {
            throw new Exception("Failed to convert text to speech from Python API");
        }

        String localFilePath = (String) responseFromPython.get("filename");
        
        String cloudinaryUrl = uploadToCloudinary(localFilePath);
        
        new File(localFilePath).delete();
        
        return cloudinaryUrl;
    }

    @Override
    public AudioRecordResponse saveAudio(SaveAudioRequest request) throws Exception {
        AudioRecord record = AudioRecord.builder()
                .text(request.getText())
                .audioUrl(request.getAudioUrl())
                .userId(request.getUserId())
                .userName(request.getUserName())
                .audioName(request.getAudioName())
                .subject(request.getSubject())
                .build();

        AudioRecord saved = audioRecordRepository.save(record);
        return AudioRecordResponse.fromEntity(saved);
    }

    @Override
    public AudioRecordResponse uploadAndSaveAudio(MultipartFile file, String audioName, String subject, Long userId, String userName) throws Exception {
        if (file.isEmpty()) {
            throw new Exception("File is empty");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("audio/")) {
            throw new Exception("File must be an audio file");
        }

        long maxFileSize = 20 * 1024 * 1024;
        if (file.getSize() > maxFileSize) {
            throw new Exception("File size exceeds maximum limit of 20MB");
        }

        String cloudinaryUrl = uploadAudioToCloudinary(file);

        AudioRecord record = AudioRecord.builder()
                .text("") // No text for uploaded audio
                .audioUrl(cloudinaryUrl)
                .userId(userId)
                .userName(userName)
                .audioName(audioName)
                .subject(subject)
                .build();

        AudioRecord saved = audioRecordRepository.save(record);
        return AudioRecordResponse.fromEntity(saved);
    }

    @Override
    public List<AudioRecordResponse> getUserAudios(Long userId) {
        return audioRecordRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(AudioRecordResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<AudioRecordResponse> getAllAudios() {
        return audioRecordRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(AudioRecordResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAudio(Long audioId) {
        AudioRecord record = audioRecordRepository.findById(audioId).orElse(null);
        if (record != null) {
            try {
                deleteFromCloudinary(record.getAudioUrl());
            } catch (Exception e) {
                System.err.println("Failed to delete from Cloudinary: " + e.getMessage());
            }
        }
        audioRecordRepository.deleteById(audioId);
    }

    private String uploadToCloudinary(String filePath) throws Exception {
        File file = new File(filePath);
        
        if (!file.exists()) {
            throw new Exception("Audio file not found: " + filePath);
        }

        Map<String, Object> uploadParams = ObjectUtils.asMap(
            "resource_type", "auto",
            "folder", cloudinaryConfig.getFolder(),
            "public_id", "tts_" + System.currentTimeMillis()
        );

        Map<String, Object> uploadResult = cloudinary.uploader().upload(file, uploadParams);
        
        return (String) uploadResult.get("secure_url");
    }

    private String uploadAudioToCloudinary(MultipartFile file) throws Exception {
        try {
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                "resource_type", "auto",
                "folder", "audio_uploads",
                "public_id", "audio_" + System.currentTimeMillis()
            );

            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                uploadParams
            );
            
            return (String) uploadResult.get("secure_url");
        } catch (Exception e) {
            throw new Exception("Failed to upload audio to Cloudinary: " + e.getMessage());
        }
    }

    private void deleteFromCloudinary(String audioUrl) throws Exception {
        if (audioUrl == null || audioUrl.isEmpty()) return;

        String[] parts = audioUrl.split("/");
        if (parts.length < 8) return;

        String publicIdWithFolder = parts[7] + "/" + parts[8].split("\\.")[0];

        Map<String, Object> deleteParams = ObjectUtils.asMap(
            "resource_type", "video"  
        );

        cloudinary.uploader().destroy(publicIdWithFolder, deleteParams);
    }
}
