package vn.edu.primary.teacher_support.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    private Long id;
    private String username;
    private String email;
    private String avatarUrl;
    private String role;
    private Boolean isActive;
}
