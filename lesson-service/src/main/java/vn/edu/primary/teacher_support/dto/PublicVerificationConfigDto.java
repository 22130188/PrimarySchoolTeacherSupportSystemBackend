package vn.edu.primary.teacher_support.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PublicVerificationConfigDto {
    private Integer minCopyCount;
    private Double minAverageRating;
    private Integer minRatingCount;
    private Integer maxOpenReports;
    private Integer minPublicDays;
    private Integer autoHideOpenReportThreshold;
}
