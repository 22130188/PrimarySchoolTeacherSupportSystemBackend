package vn.edu.primary.teacher_support.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public class RegisterRequest {

    @NotBlank(message = "Username không được để trống")
    private String username;
    @NotBlank(message = "Email không được để trống")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@gmail\\.com$", message = "Email không hợp lệ")
    private String email;
    @NotBlank(message = "Password không được để trống")
    @Size(min = 6, message = "Mật khẩu phải trên 6 ký tự")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[@#$%^&+=!]).{6,}$", message = "Mật khẩu phải có chữ hoa và ký tự đặc biệt")
    private String password;
    @NotBlank(message = "Role không được để trống")
    private String role;
    private String schoolName;
    private String grade;
    private List<TeacherClassDto> classes;

    public static class TeacherClassDto {
        private String grade;
        private String subject;

        public String getGrade() {
            return grade;
        }

        public void setGrade(String grade) {
            this.grade = grade;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public List<TeacherClassDto> getClasses() {
        return classes;
    }

    public void setClasses(List<TeacherClassDto> classes) {
        this.classes = classes;
    }
}