package vn.edu.primary.teacher_support.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import vn.edu.primary.teacher_support.entity.enums.PublicReportStatus;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ResolvePublicReportRequest {
    @NotNull
    private PublicReportStatus status;
    private String adminNote;
}
