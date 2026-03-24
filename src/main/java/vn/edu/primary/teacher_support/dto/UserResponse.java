package vn.edu.primary.teacher_support.dto;

import lombok.*;
import vn.edu.primary.teacher_support.entity.Role;
import vn.edu.primary.teacher_support.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String avatarUrl;
    private String phone;
    private LocalDate dateOfBirth;
    private String schoolName;
    private String role;
    private Boolean isActive;
    private Boolean isEmailVerified;
    private String grade;
    private List<TeacherClassDto> teacherClasses;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        String primaryRole = user.getRoles().stream()
                .map(Role::getName)
                .max(Comparator.comparingInt(UserResponse::rolePriority))
                .map(Enum::name)
                .orElse(null);

        String grade = null;
        if (user.getStudentInfo() != null) {
            grade = user.getStudentInfo().getGrade();
        }

        List<TeacherClassDto> teacherClasses = null;
        if (user.getTeacherClasses() != null && !user.getTeacherClasses().isEmpty()) {
            teacherClasses = user.getTeacherClasses().stream()
                    .map(tc -> new TeacherClassDto(tc.getGrade(), tc.getSubject()))
                    .toList();
        }

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .phone(user.getPhone())
                .dateOfBirth(user.getDateOfBirth())
                .schoolName(user.getSchoolName())
                .role(primaryRole)
                .isActive(user.getIsActive())
                .isEmailVerified(user.getIsEmailVerified())
                .grade(grade)
                .teacherClasses(teacherClasses)
                .createdAt(user.getCreatedAt())
                .build();
    }

    private static int rolePriority(Role.RoleName roleName) {
        return switch (roleName) {
            case STUDENT -> 0;
            case TEACHER -> 1;
            case ADMIN -> 2;
        };
    }
}
