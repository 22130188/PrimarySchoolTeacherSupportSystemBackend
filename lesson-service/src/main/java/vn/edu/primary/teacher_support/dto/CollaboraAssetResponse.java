package vn.edu.primary.teacher_support.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollaboraAssetResponse {
    private String assetId;
    private String fileName;
    private String url;
}
