package vn.edu.primary.tts.service;

import vn.edu.primary.tts.dto.TTSConvertRequest;
import vn.edu.primary.tts.dto.SaveAudioRequest;
import vn.edu.primary.tts.dto.AudioRecordResponse;
import java.util.List;

public interface TTSService {
    String convertTextToSpeech(TTSConvertRequest request) throws Exception;
    AudioRecordResponse saveAudio(SaveAudioRequest request) throws Exception;
    List<AudioRecordResponse> getUserAudios(Long userId);
    void deleteAudio(Long audioId);
}
