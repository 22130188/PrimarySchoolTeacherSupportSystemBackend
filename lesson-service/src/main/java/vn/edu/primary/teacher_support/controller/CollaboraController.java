package vn.edu.primary.teacher_support.controller;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.primary.teacher_support.dto.CollaboraAssetResponse;
import vn.edu.primary.teacher_support.dto.CreateCollaboraDraftRequest;
import vn.edu.primary.teacher_support.dto.CollaboraEditorSessionResponse;
import vn.edu.primary.teacher_support.dto.DraftResponse;
import vn.edu.primary.teacher_support.dto.TranslateCollaboraDraftRequest;
import vn.edu.primary.teacher_support.service.AuthHelper;
import vn.edu.primary.teacher_support.service.CollaboraSessionService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CollaboraController {

    private final CollaboraSessionService collaboraSessionService;
    private final AuthHelper authHelper;

    @Value("${collabora.post-message-origin:http://localhost:5173}")
    private String postMessageOrigin;

    @PostMapping("/api/lessons/drafts/collabora/drafts")
    public ResponseEntity<DraftResponse> createDraft(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody CreateCollaboraDraftRequest request
    ) {
        Long userId = authHelper.extractUserId(authorization);
        return ResponseEntity.ok(collaboraSessionService.createDraft(
                userId,
                request.getTitle(),
                request.getSubject(),
                request.getGrade(),
                request.getVolume(),
                request.getBook(),
                request.getType()
        ));
    }

    @PostMapping(value = "/api/lessons/drafts/collabora/drafts/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DraftResponse> uploadDraft(
            @RequestHeader("Authorization") String authorization,
            @RequestParam("file") MultipartFile file,
            @RequestParam String title,
            @RequestParam String subject,
            @RequestParam String grade,
            @RequestParam(required = false) String volume,
            @RequestParam(required = false) String book
    ) {
        Long userId = authHelper.extractUserId(authorization);
        return ResponseEntity.ok(collaboraSessionService.uploadDraft(userId, file, title, subject, grade, volume, book));
    }

    @PostMapping("/api/lessons/drafts/collabora/drafts/{draftId}/translate")
    public ResponseEntity<DraftResponse> translateDraft(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long draftId,
            @Valid @RequestBody TranslateCollaboraDraftRequest request
    ) {
        Long userId = authHelper.extractUserId(authorization);
        return ResponseEntity.ok(collaboraSessionService.translateDraft(
                userId,
                draftId,
                request.getEffectiveSourceLang(),
                request.getEffectiveTargetLang(),
                request.getTitle()
        ));
    }
    @GetMapping("/api/lessons/drafts/collabora/drafts/{draftId}/editor")
    public ResponseEntity<CollaboraEditorSessionResponse> getEditorSession(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long draftId,
            @RequestParam(required = false) Long classroomId
    ) {
        Long userId = authHelper.extractUserId(authorization);
        if (classroomId != null) {
            return ResponseEntity.ok(collaboraSessionService.getClassroomEditorSession(userId, classroomId, draftId));
        }
        return ResponseEntity.ok(collaboraSessionService.getEditorSession(userId, draftId));
    }

    @GetMapping("/api/lessons/drafts/collabora/classrooms/{classroomId}/drafts/{draftId}/editor")
    public ResponseEntity<CollaboraEditorSessionResponse> getClassroomEditorSession(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long classroomId,
            @PathVariable Long draftId
    ) {
        Long userId = authHelper.extractUserId(authorization);
        return ResponseEntity.ok(collaboraSessionService.getClassroomEditorSession(userId, classroomId, draftId));
    }

    @PostMapping(value = "/api/lessons/drafts/collabora/assets", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CollaboraAssetResponse> createAsset(
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false) String sourceUrl
    ) {
        return ResponseEntity.ok(collaboraSessionService.createImageAsset(file, sourceUrl));
    }

    @GetMapping("/api/lessons/drafts/collabora/assets/{assetId}")
    public ResponseEntity<byte[]> getAsset(@PathVariable String assetId) {
        CollaboraSessionService.CollaboraAsset asset = collaboraSessionService.getAsset(assetId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(asset.getContentType()))
                .body(asset.getContent());
    }

    @GetMapping("/wopi/files/{fileId}")
    public ResponseEntity<?> checkFileInfo(
            @PathVariable String fileId,
            @RequestParam("access_token") String accessToken
    ) {
        try {
            CollaboraSessionService.CollaboraFileSession session =
                    collaboraSessionService.requireSession(fileId, accessToken);

            Map<String, Object> info = new HashMap<>();
            info.put("BaseFileName", session.getFileName());
            info.put("OwnerId", String.valueOf(session.getUserId()));
            info.put("Size", collaboraSessionService.fileSize(session));
            info.put("UserId", String.valueOf(session.getUserId()));
            info.put("UserFriendlyName", "Teacher");
            info.put("Version", String.valueOf(System.currentTimeMillis()));
            info.put("UserCanWrite", session.isCanWrite());
            info.put("UserCanNotWriteRelative", !session.isCanWrite());
            info.put("UserCanRename", false);
            info.put("DisablePrint", false);
            info.put("DisableExport", false);
            info.put("DisableCopy", false);
            info.put("PostMessageOrigin", postMessageOrigin);
            return ResponseEntity.ok(info);
        } catch (CollaboraSessionService.CollaboraUnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/wopi/files/{fileId}/contents")
    public ResponseEntity<?> getFile(
            @PathVariable String fileId,
            @RequestParam("access_token") String accessToken
    ) {
        try {
            CollaboraSessionService.CollaboraFileSession session =
                    collaboraSessionService.requireSession(fileId, accessToken);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(collaboraSessionService.readFile(session));
        } catch (CollaboraSessionService.CollaboraUnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/wopi/files/{fileId}/contents")
    public ResponseEntity<?> putFile(
            @PathVariable String fileId,
            @RequestParam("access_token") String accessToken,
            @RequestBody byte[] content
    ) {
        try {
            CollaboraSessionService.CollaboraFileSession session =
                    collaboraSessionService.requireSession(fileId, accessToken);
            collaboraSessionService.requireWritable(session);
            collaboraSessionService.writeFile(session, content);
            return ResponseEntity.ok().build();
        } catch (CollaboraSessionService.CollaboraUnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
