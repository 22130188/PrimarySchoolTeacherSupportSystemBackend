package vn.edu.primary.tts.service;

import vn.edu.primary.tts.dto.TTSConvertRequest;
import vn.edu.primary.tts.dto.SaveAudioRequest;
import vn.edu.primary.tts.dto.AudioRecordResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

public interface TTSService {
    String convertTextToSpeech(TTSConvertRequest request) throws Exception;
    AudioRecordResponse saveAudio(SaveAudioRequest request) throws Exception;
    AudioRecordResponse uploadAndSaveAudio(MultipartFile file, String audioName, String subject, Long userId, String userName) throws Exception;
    List<AudioRecordResponse> getUserAudios(Long userId);
    List<AudioRecordResponse> getAllAudios();
    void deleteAudio(Long audioId);
}
