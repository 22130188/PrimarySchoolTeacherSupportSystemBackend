package vn.edu.primary.image.service.impl;

import vn.edu.primary.image.config.CloudinaryConfig;
import vn.edu.primary.image.dto.ImageGenerateRequest;
import vn.edu.primary.image.dto.SaveImageRequest;
import vn.edu.primary.image.dto.ImageRecordResponse;
import vn.edu.primary.image.entity.ImageRecord;
import vn.edu.primary.image.repository.ImageRecordRepository;
import vn.edu.primary.image.service.ImageService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ImageServiceImpl implements ImageService {

    @Autowired
    private ImageRecordRepository imageRecordRepository;

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private CloudinaryConfig cloudinaryConfig;

    @Value("${python.image.api-url}")
    private String pythonImageApiUrl;

    private final RestTemplate restTemplate;

    public ImageServiceImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public String generateImage(ImageGenerateRequest request) throws Exception {
        String pythonEndpoint = pythonImageApiUrl;
        if (pythonEndpoint.endsWith("/")) {
            pythonEndpoint = pythonEndpoint.substring(0, pythonEndpoint.length() - 1);
        }
        if (pythonEndpoint.endsWith("/image")) {
            pythonEndpoint += "/generate";
        } else {
            pythonEndpoint += "/image/generate";
        }

        Map<String, Object> responseFromPython = restTemplate.postForObject(
            pythonEndpoint,
            request,
            Map.class
        );

        if (responseFromPython == null || !responseFromPython.containsKey("filename")) {
            throw new Exception("Failed to generate image from Python API");
        }

        String localFilePath = (String) responseFromPython.get("filename");

        String cloudinaryUrl = uploadToCloudinary(localFilePath);

        new File(localFilePath).delete();

        return cloudinaryUrl;
    }

    @Override
    public ImageRecordResponse saveImage(SaveImageRequest request) throws Exception {
        ImageRecord record = ImageRecord.builder()
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .userId(request.getUserId())
                .userName(request.getUserName())
                .subject(request.getSubject())
                .build();

        ImageRecord saved = imageRecordRepository.save(record);
        return ImageRecordResponse.fromEntity(saved);
    }

    @Override
    public List<ImageRecordResponse> getUserImages(Long userId) {
        return imageRecordRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(ImageRecordResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ImageRecordResponse> getAllImages() {
        return imageRecordRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(ImageRecordResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteImage(Long imageId) {
        ImageRecord record = imageRecordRepository.findById(imageId).orElse(null);
        if (record != null) {
            // Delete from Cloudinary
            try {
                deleteFromCloudinary(record.getImageUrl());
            } catch (Exception e) {
                // Log error but continue with DB deletion
                System.err.println("Failed to delete from Cloudinary: " + e.getMessage());
            }
        }
        imageRecordRepository.deleteById(imageId);
    }

    private String uploadToCloudinary(String filePath) throws Exception {
        File file = new File(filePath);

        if (!file.exists()) {
            throw new Exception("Image file not found: " + filePath);
        }

        Map<String, Object> uploadParams = ObjectUtils.asMap(
            "resource_type", "auto",
            "folder", cloudinaryConfig.getFolder(),
            "public_id", "image_" + System.currentTimeMillis()
        );

        Map<String, Object> uploadResult = cloudinary.uploader().upload(file, uploadParams);

        return (String) uploadResult.get("secure_url");
    }

    private void deleteFromCloudinary(String imageUrl) throws Exception {
        String publicId = extractCloudinaryPublicId(imageUrl);
        if (publicId == null || publicId.isBlank()) return;

        Map<String, Object> deleteParams = ObjectUtils.asMap(
            "resource_type", "image"
        );

        cloudinary.uploader().destroy(publicId, deleteParams);
    }

    private String extractCloudinaryPublicId(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank() || !imageUrl.contains("/upload/")) {
            return null;
        }

        String path = imageUrl.split("\\?", 2)[0];
        String[] uploadParts = path.split("/upload/", 2);
        if (uploadParts.length < 2 || uploadParts[1].isBlank()) {
            return null;
        }

        String publicPath = uploadParts[1];
        publicPath = publicPath.replaceFirst("^v\\d+/", "");

        int extensionIndex = publicPath.lastIndexOf('.');
        if (extensionIndex > publicPath.lastIndexOf('/')) {
            publicPath = publicPath.substring(0, extensionIndex);
        }

        return URLDecoder.decode(publicPath, StandardCharsets.UTF_8);
    }
}
