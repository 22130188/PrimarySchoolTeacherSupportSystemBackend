package vn.edu.primary.teacher_support.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import vn.edu.primary.teacher_support.json.MultiFormatLocalDateDeserializer;

import java.time.LocalDate;

@Data
public class UpdatePersonalRequest {
    /** Họ và tên hiển thị — không phải tên đăng nhập. */
    @NotBlank(message = "Họ và tên không được để trống")
    @Size(min = 2, max = 100, message = "Họ và tên phải từ 2-100 ký tự")
    private String fullName;

    @NotNull(message = "Ngày sinh không được để trống")
    @JsonDeserialize(using = MultiFormatLocalDateDeserializer.class)
    private LocalDate dateOfBirth;

    private String gender;

    private String position;
    @NotBlank(message = "Email không được để trống")
    private String email;

    private String phone;
}