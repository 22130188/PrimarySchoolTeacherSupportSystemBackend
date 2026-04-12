package vn.edu.primary.image.service;

import vn.edu.primary.image.dto.ImageGenerateRequest;
import vn.edu.primary.image.dto.SaveImageRequest;
import vn.edu.primary.image.dto.ImageRecordResponse;
import java.util.List;

public interface ImageService {
    String generateImage(ImageGenerateRequest request) throws Exception;
    ImageRecordResponse saveImage(SaveImageRequest request) throws Exception;
    List<ImageRecordResponse> getUserImages(Long userId);
    List<ImageRecordResponse> getAllImages();
    void deleteImage(Long imageId);
}