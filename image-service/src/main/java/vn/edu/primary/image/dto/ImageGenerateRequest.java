package vn.edu.primary.image.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageGenerateRequest {

    @NotBlank(message = "Description không được để trống")
    @Size(max = 1000, message = "Description không được vượt quá 1000 ký tự")
    private String description;
}