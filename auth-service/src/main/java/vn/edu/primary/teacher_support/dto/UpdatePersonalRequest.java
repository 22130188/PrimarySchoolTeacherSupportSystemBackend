package vn.edu.primary.teacher_support.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UpdatePersonalRequest {
    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, max = 100, message = "Username phải từ 3-100 ký tự")
    private String username;

    @NotNull(message = "Ngày sinh không được để trống")
    private LocalDate dateOfBirth;

    private String gender;

    private String position;
    @NotBlank(message = "Email không được để trống")
    private String email;

    private String phone;
}