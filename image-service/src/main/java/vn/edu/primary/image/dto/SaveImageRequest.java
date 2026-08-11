package vn.edu.primary.image.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveImageRequest {

    @NotBlank(message = "Description không được để trống")
    private String description;

    @NotBlank(message = "Image URL không được để trống")
    private String imageUrl;

    @NotNull(message = "User ID không được để trống")
    private Long userId;

    private String userName;
    private String subject;
    private String grade;
}
