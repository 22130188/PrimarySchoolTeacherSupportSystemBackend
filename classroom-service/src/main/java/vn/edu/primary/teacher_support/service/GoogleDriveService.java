package vn.edu.primary.teacher_support.service;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import vn.edu.primary.teacher_support.dto.DriveAttachmentRequest;
import vn.edu.primary.teacher_support.exception.BusinessException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleDriveService {

    private static final Pattern FILE_D_PATTERN = Pattern.compile("/file/d/([a-zA-Z0-9_-]+)");
    private static final Pattern OPEN_ID_PATTERN = Pattern.compile("[?&]id=([a-zA-Z0-9_-]+)");
    private static final Pattern DOC_D_PATTERN = Pattern.compile("/d/([a-zA-Z0-9_-]+)");

    @Value("${google.drive.api-key:}")
    private String driveApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public ResolvedDriveAttachment resolveAttachment(DriveAttachmentRequest request) {
        if (request == null) {
            throw new BusinessException("Thông tin file Drive không hợp lệ");
        }

        String fileId = extractFileId(request);
        if (fileId == null || fileId.isBlank()) {
            throw new BusinessException("Không tìm thấy Google Drive fileId từ dữ liệu gửi lên");
        }

        Map<String, Object> metadata = Collections.emptyMap();
        if (driveApiKey != null && !driveApiKey.isBlank()) {
            metadata = fetchMetadata(fileId);
        } else {
            log.debug("GOOGLE_DRIVE_API_KEY is missing. Skip metadata lookup for fileId={}", fileId);
        }

        String name = safeString(metadata.get("name"));
        String mimeType = safeString(metadata.get("mimeType"));
        Long sizeBytes = parseLong(metadata.get("size"));
        String iconLink = safeString(metadata.get("iconLink"));
        String thumbnailLink = safeString(metadata.get("thumbnailLink"));
        String webViewLink = safeString(metadata.get("webViewLink"));
        String webContentLink = safeString(metadata.get("webContentLink"));

        if ((name == null || name.isBlank()) && request.getTitle() != null && !request.getTitle().isBlank()) {
            name = request.getTitle().trim();
        }
        if (name == null || name.isBlank()) {
            name = "Drive File";
        }

        if (webViewLink == null || webViewLink.isBlank()) {
            if (request.getDriveUrl() != null && !request.getDriveUrl().isBlank()) {
                webViewLink = request.getDriveUrl().trim();
            } else {
                webViewLink = "https://drive.google.com/file/d/" + fileId + "/view";
            }
        }

        return ResolvedDriveAttachment.builder()
                .driveFileId(fileId)
                .name(name)
                .mimeType(mimeType)
                .sizeBytes(sizeBytes)
                .iconLink(iconLink)
                .thumbnailLink(thumbnailLink)
                .webViewLink(webViewLink)
                .webContentLink(webContentLink)
                .build();
    }

    private Map<String, Object> fetchMetadata(String fileId) {
        String encodedFileId = URLEncoder.encode(fileId, StandardCharsets.UTF_8);
        String encodedKey = URLEncoder.encode(driveApiKey, StandardCharsets.UTF_8);

        String url = "https://www.googleapis.com/drive/v3/files/" + encodedFileId
                + "?fields=id,name,mimeType,size,iconLink,thumbnailLink,webViewLink,webContentLink"
                + "&supportsAllDrives=true&key=" + encodedKey;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = restTemplate.getForObject(url, Map.class);
            if (body == null || body.get("id") == null) {
                log.debug("Google Drive metadata is empty for fileId={}", fileId);
                return Collections.emptyMap();
            }
            return body;
        } catch (RestClientException ex) {
            // Private files or domain-restricted files can fail metadata lookup with API key.
            // We still allow posting the original link and keep attachment data minimal.
            log.debug("Cannot fetch Google Drive metadata for fileId={}", fileId, ex);
            return Collections.emptyMap();
        }
    }

    private String extractFileId(DriveAttachmentRequest request) {
        if (request.getFileId() != null && !request.getFileId().isBlank()) {
            return request.getFileId().trim();
        }

        String driveUrl = request.getDriveUrl();
        if (driveUrl == null || driveUrl.isBlank()) {
            return null;
        }

        String normalized = driveUrl.trim();

        Matcher fileDMatcher = FILE_D_PATTERN.matcher(normalized);
        if (fileDMatcher.find()) {
            return fileDMatcher.group(1);
        }

        Matcher openIdMatcher = OPEN_ID_PATTERN.matcher(normalized);
        if (openIdMatcher.find()) {
            return openIdMatcher.group(1);
        }

        Matcher docMatcher = DOC_D_PATTERN.matcher(normalized);
        if (docMatcher.find()) {
            return docMatcher.group(1);
        }

        return null;
    }

    private String safeString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @Getter
    @Builder
    public static class ResolvedDriveAttachment {
        private String driveFileId;
        private String name;
        private String mimeType;
        private Long sizeBytes;
        private String iconLink;
        private String thumbnailLink;
        private String webViewLink;
        private String webContentLink;
    }
}
