package vn.edu.primary.image.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageRecordResponse {

    private Long id;
    private String description;
    private String imageUrl;
    private Long userId;
    private String userName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ImageRecordResponse fromEntity(vn.edu.primary.image.entity.ImageRecord entity) {
        return ImageRecordResponse.builder()
                .id(entity.getId())
                .description(entity.getDescription())
                .imageUrl(entity.getImageUrl())
                .userId(entity.getUserId())
                .userName(entity.getUserName())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}