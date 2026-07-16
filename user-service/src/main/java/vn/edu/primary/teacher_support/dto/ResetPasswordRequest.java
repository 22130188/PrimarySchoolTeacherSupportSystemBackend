package vn.edu.primary.teacher_support.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ResetPasswordRequest(
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        String email,

        @NotBlank(message = "Mã đặt lại mật khẩu không được để trống")
        String resetToken,

        @NotBlank(message = "Mật khẩu mới không được để trống")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[@#$%^&+=!]).{6,}$",
                message = "Mật khẩu cần ít nhất 6 ký tự, có chữ hoa và ký tự đặc biệt"
        )
        String newPassword
) {}
