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
    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, max = 100, message = "Username phải từ 3-100 ký tự")
    private String username;

    @NotNull(message = "Ngày sinh không được để trống")
    @JsonDeserialize(using = MultiFormatLocalDateDeserializer.class)
    private LocalDate dateOfBirth;

    private String gender;

    private String position;
    @NotBlank(message = "Email không được để trống")
    private String email;

    private String phone;
}