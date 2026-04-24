package vn.edu.primary.teacher_support.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UserDto {
    private Long id;
    private String email;
    private String fullName;
    private String role;
}
