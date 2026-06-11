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
public class CollaboraEditorSessionResponse {
    private Long draftId;
    private String fileId;
    private String fileName;
    private String actionUrl;
    private String accessToken;
    private String accessTokenTtl;
    private Boolean canWrite;
}
