package vn.edu.primary.teacher_support.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.edu.primary.teacher_support.entity.enums.LessonTemplateStatus;

@Getter
@Setter
@NoArgsConstructor
public class UpdateLessonTemplateRequest {
    private String title;
    private String description;
    private String subject;
    private String grade;
    private LessonTemplateStatus status;
}
