package vn.edu.primary.teacher_support.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageDto {
    private Integer id;
    private Integer pageNumber;
    private String imageUrl;
}
