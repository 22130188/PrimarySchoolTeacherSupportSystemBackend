package vn.edu.primary.teacher_support.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinByLinkRequest {

    @NotBlank(message = "Invite link token không được để trống")
    private String token;
}
