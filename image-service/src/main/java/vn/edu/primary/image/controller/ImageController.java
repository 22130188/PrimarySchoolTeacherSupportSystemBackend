package vn.edu.primary.image.controller;

import vn.edu.primary.image.dto.ImageGenerateRequest;
import vn.edu.primary.image.dto.SaveImageRequest;
import vn.edu.primary.image.dto.ImageRecordResponse;
import vn.edu.primary.image.dto.ApiResponse;
import vn.edu.primary.image.service.ImageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("")
@CrossOrigin("*")
public class ImageController {

    @Autowired
    private ImageService imageService;

    @PostMapping("/generate")
    public ResponseEntity<?> generateImage(
            @Valid @RequestBody ImageGenerateRequest request,
            HttpServletRequest httpRequest) {
        try {
            String imageUrl = imageService.generateImage(request);

            Map<String, Object> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            response.put("description", request.getDescription());

            return ResponseEntity.ok(ApiResponse.success("Tạo ảnh thành công", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Lỗi tạo ảnh: " + e.getMessage()));
        }
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveImage(@Valid @RequestBody SaveImageRequest request) {
        try {
            ImageRecordResponse result = imageService.saveImage(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Lưu ảnh thành công", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Lỗi lưu ảnh: " + e.getMessage()));
        }
    }

    @GetMapping("/images/{userId}")
    public ResponseEntity<?> getUserImages(@PathVariable Long userId) {
        try {
            List<ImageRecordResponse> images = imageService.getUserImages(userId);
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thành công", images));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Lỗi lấy danh sách: " + e.getMessage()));
        }
    }

    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<?> deleteImage(@PathVariable Long imageId) {
        try {
            imageService.deleteImage(imageId);
            return ResponseEntity.ok(ApiResponse.success("Xóa ảnh thành công", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Lỗi xóa ảnh: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(ApiResponse.success("Image Service is running", null));
    }
}