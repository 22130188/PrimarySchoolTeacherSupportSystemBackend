package vn.edu.primary.teacher_support.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookDto {
    private Integer id;
    private String title;
    private Integer grade;
    private String subject;
    private String coverUrl;
    private String slugId;
    private String url;
    private String bookType;
    private List<PageDto> pages;
}
