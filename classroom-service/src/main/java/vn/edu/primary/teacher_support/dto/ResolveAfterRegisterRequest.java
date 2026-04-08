package vn.edu.primary.teacher_support.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResolveAfterRegisterRequest {

    private String email;
    private Long userId;
}
