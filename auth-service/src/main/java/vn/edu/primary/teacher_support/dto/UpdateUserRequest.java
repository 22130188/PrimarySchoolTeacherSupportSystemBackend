package vn.edu.primary.teacher_support.dto;

import jakarta.validation.constraints.Email;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    private String username;

    @Email(message = "Email không hợp lệ")
    private String email;

    private String password;

    private String role;
    private String phone;
    private LocalDate dateOfBirth;
    private String schoolName;
    private String avatarUrl;

    private String grade;
    private List<TeacherClassDto> teacherClasses;
}
