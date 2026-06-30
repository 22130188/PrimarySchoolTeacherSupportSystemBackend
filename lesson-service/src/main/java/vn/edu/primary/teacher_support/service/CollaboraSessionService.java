package vn.edu.primary.teacher_support.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import vn.edu.primary.teacher_support.dto.CollaboraEditorSessionResponse;
import vn.edu.primary.teacher_support.dto.CollaboraAssetResponse;
import vn.edu.primary.teacher_support.dto.DraftResponse;
import vn.edu.primary.teacher_support.entity.LessonClassroomShare;
import vn.edu.primary.teacher_support.entity.LessonDraft;
import vn.edu.primary.teacher_support.entity.LessonShare;
import vn.edu.primary.teacher_support.entity.LessonTemplate;
import vn.edu.primary.teacher_support.entity.enums.LessonTemplateStatus;
import vn.edu.primary.teacher_support.exception.BusinessException;
import vn.edu.primary.teacher_support.exception.ForbiddenException;
import vn.edu.primary.teacher_support.exception.ResourceNotFoundException;
import vn.edu.primary.teacher_support.repository.LessonClassroomShareRepository;
import vn.edu.primary.teacher_support.repository.LessonShareRepository;
import vn.edu.primary.teacher_support.repository.LessonDraftRepository;
import vn.edu.primary.teacher_support.repository.LessonTemplateRepository;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class CollaboraSessionService {

    private static final List<String> ALLOWED_TYPES = List.of("COLLABORA_DOCX", "COLLABORA_PPTX");
    private static final List<String> ALLOWED_EXTENSIONS = List.of("docx", "pptx");

    private final LessonDraftRepository draftRepository;
    private final LessonClassroomShareRepository classroomShareRepository;
    private final LessonShareRepository lessonShareRepository;
    private final ClassroomServiceClient classroomServiceClient;
    private final LessonTemplateRepository templateRepository;
    private final SupabaseStorageService supabaseStorageService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Map<String, String> actionUrlCache = new ConcurrentHashMap<>();
    private final Map<String, CollaboraFileSession> activeSessions = new ConcurrentHashMap<>();
    private final Map<String, CollaboraAsset> assets = new ConcurrentHashMap<>();

    @Value("${collabora.discovery-url:http://localhost:9980/hosting/discovery}")
    private String discoveryUrl;

    @Value("${collabora.wopi-public-url:http://host.docker.internal:8087/wopi/files}")
    private String wopiPublicUrl;

    @Value("${collabora.asset-public-url:http://host.docker.internal:8087/api/lessons/drafts/collabora/assets}")
    private String assetPublicUrl;

    public DraftResponse createDraft(Long userId, String title, String subject, String grade, String volume, String book, String type) {
        String normalizedType = normalizeType(type);
        String extension = extensionForType(normalizedType);
        String fileId = "lesson-" + userId + "-" + UUID.randomUUID() + "." + extension;
        String fileName = safeFileName(title, extension);

        try {
            byte[] template = new ClassPathResource("collabora-templates/blank." + extension)
                    .getInputStream()
                    .readAllBytes();
            supabaseStorageService.uploadFile(fileId, template, contentType(extension));

            LessonDraft draft = LessonDraft.builder()
                    .userId(userId)
                    .title(stripExtension(fileName, extension))
                    .subject(subject)
                    .grade(grade)
                    .volume(blankToNull(volume))
                    .book(blankToNull(book))
                    .type(normalizedType)
                    .canvasJson(objectMapper.writeValueAsString(Map.of(
                            "collaboraFileId", fileId,
                            "fileName", fileName,
                            "extension", extension
                    )))
                    .build();
            draft = draftRepository.save(draft);
            return toResponse(draft);
        } catch (Exception e) {
            throw new BusinessException("Khong the tao bai giang Collabora tren Supabase: " + e.getMessage());
        }
    }

    public DraftResponse uploadDraft(Long userId, MultipartFile file, String title, String subject, String grade, String volume, String book) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Vui long chon file de tai len");
        }
        if (title == null || title.isBlank()) {
            throw new BusinessException("Tieu de khong duoc de trong");
        }
        if (subject == null || subject.isBlank()) {
            throw new BusinessException("Mon hoc khong duoc de trong");
        }
        if (grade == null || grade.isBlank()) {
            throw new BusinessException("Lop khong duoc de trong");
        }

        String originalName = file.getOriginalFilename();
        String extension = extensionFromFileName(originalName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("Collabora chi ho tro file .docx hoac .pptx");
        }

        String type = "pptx".equals(extension) ? "COLLABORA_PPTX" : "COLLABORA_DOCX";
        String fileId = "lesson-" + userId + "-" + UUID.randomUUID() + "." + extension;
        String fileName = safeFileName(title, extension);

        try {
            supabaseStorageService.uploadFile(fileId, file.getBytes(), contentType(extension));

            LessonDraft draft = LessonDraft.builder()
                    .userId(userId)
                    .title(stripExtension(fileName, extension))
                    .subject(subject)
                    .grade(grade)
                    .volume(blankToNull(volume))
                    .book(blankToNull(book))
                    .type(type)
                    .canvasJson(objectMapper.writeValueAsString(Map.of(
                            "collaboraFileId", fileId,
                            "fileName", fileName,
                            "extension", extension
                    )))
                    .build();
            draft = draftRepository.save(draft);
            return toResponse(draft);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Khong the tai file Collabora len Supabase: " + e.getMessage());
        }
    }

    public CollaboraEditorSessionResponse getEditorSession(Long userId, Long draftId) {
        LessonDraft ownedDraft = draftRepository.findByIdAndUserId(draftId, userId).orElse(null);
        if (ownedDraft != null) {
            if (!ALLOWED_TYPES.contains(ownedDraft.getType())) {
                throw new BusinessException("Bai giang nay khong phai dinh dang Collabora");
            }
            return buildEditorSession(userId, draftId, ownedDraft, true, true);
        }

        // Khong phai chu so huu: cho phep neu bai giang da duoc chia se truc tiep cho giao vien nay (chi xem)
        lessonShareRepository.findByDraftIdAndSharedWithUserId(draftId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay bai giang Collabora"));

        LessonDraft draft = draftRepository.findById(draftId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay bai giang Collabora"));
        if (!ALLOWED_TYPES.contains(draft.getType())) {
            throw new BusinessException("Bai giang nay khong phai dinh dang Collabora");
        }

        return buildEditorSession(userId, draftId, draft, false, false);
    }

    public CollaboraEditorSessionResponse getClassroomEditorSession(Long userId, Long classroomId, Long draftId) {
        if (!classroomServiceClient.hasAccess(classroomId, userId)) {
            throw new ForbiddenException("Ban khong co quyen truy cap lop hoc nay");
        }

        LessonClassroomShare share = classroomShareRepository.findByDraftIdAndClassroomId(draftId, classroomId)
                .orElseThrow(() -> new ForbiddenException("Bai giang khong duoc chia se trong lop nay"));

        LessonDraft draft = draftRepository.findById(draftId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay bai giang Collabora da chia se trong lop"));
        if (!ALLOWED_TYPES.contains(draft.getType())) {
            throw new BusinessException("Bai giang nay khong phai dinh dang Collabora");
        }

        boolean canWrite = share.getOwnerUserId().equals(userId);
        return buildEditorSession(userId, draftId, draft, canWrite, canWrite);
    }

    public CollaboraEditorSessionResponse getTemplateEditorSession(Long userId, Long templateId) {
        LessonTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay mau bai giang"));
        if (template.getStatus() != LessonTemplateStatus.ACTIVE) {
            throw new BusinessException("Mau bai giang nay dang tam an");
        }
        if (!ALLOWED_TYPES.contains(template.getType()) || !ALLOWED_EXTENSIONS.contains(template.getExtension())) {
            throw new BusinessException("Mau bai giang khong hop le");
        }
        return buildTemplateEditorSession(userId, template);
    }

    private CollaboraEditorSessionResponse buildEditorSession(
            Long userId,
            Long draftId,
            LessonDraft draft,
            boolean canWrite,
            boolean canPersist
    ) {
        CollaboraMetadata metadata = readMetadata(draft);
        String accessToken = UUID.randomUUID().toString();
        activeSessions.put(sessionKey(metadata.fileId(), accessToken), new CollaboraFileSession(
                metadata.fileId(),
                metadata.fileName(),
                metadata.extension(),
                userId,
                draftId,
                accessToken,
                canWrite,
                canPersist
        ));

        String actionUrl = resolveActionUrl(metadata.extension(), canWrite ? "edit" : "view");
        String wopiSrc = wopiPublicUrl.replaceAll("/+$", "") + "/" +
                URLEncoder.encode(metadata.fileId(), StandardCharsets.UTF_8).replace("+", "%20");
        String fullActionUrl = actionUrl + "?WOPISrc=" + URLEncoder.encode(wopiSrc, StandardCharsets.UTF_8);

        return CollaboraEditorSessionResponse.builder()
                .draftId(draftId)
                .fileId(metadata.fileId())
                .fileName(metadata.fileName())
                .actionUrl(fullActionUrl)
                .accessToken(accessToken)
                .accessTokenTtl("0")
                .canWrite(canWrite)
                .build();
    }

    private CollaboraEditorSessionResponse buildTemplateEditorSession(Long userId, LessonTemplate template) {
        String accessToken = UUID.randomUUID().toString();
        activeSessions.put(sessionKey(template.getFileId(), accessToken), new CollaboraFileSession(
                template.getFileId(),
                template.getFileName(),
                template.getExtension(),
                userId,
                null,
                accessToken,
                false,
                false
        ));

        String actionUrl = resolveActionUrl(template.getExtension(), "view");
        String wopiSrc = wopiPublicUrl.replaceAll("/+$", "") + "/" +
                URLEncoder.encode(template.getFileId(), StandardCharsets.UTF_8).replace("+", "%20");
        String fullActionUrl = actionUrl + "?WOPISrc=" + URLEncoder.encode(wopiSrc, StandardCharsets.UTF_8);

        return CollaboraEditorSessionResponse.builder()
                .draftId(null)
                .fileId(template.getFileId())
                .fileName(template.getFileName())
                .actionUrl(fullActionUrl)
                .accessToken(accessToken)
                .accessTokenTtl("0")
                .canWrite(false)
                .build();
    }

    public CollaboraAssetResponse createImageAsset(MultipartFile file, String sourceUrl) {
        try {
            byte[] content;
            String contentType;
            String fileName;

            if (file != null && !file.isEmpty()) {
                content = file.getBytes();
                contentType = file.getContentType();
                fileName = file.getOriginalFilename();
            } else if (sourceUrl != null && !sourceUrl.isBlank()) {
                URI uri = parseImageSourceUrl(sourceUrl);
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(uri)
                        .timeout(Duration.ofSeconds(20))
                        .header("User-Agent", "Mozilla/5.0")
                        .GET()
                        .build();
                HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new BusinessException("Khong the tai anh nguon");
                }
                content = response.body();
                contentType = response.headers().firstValue("content-type").orElse("image/png");
                fileName = "image." + extensionFromContentType(contentType);
            } else {
                throw new BusinessException("Khong co anh de chen vao Collabora");
            }

            if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
                throw new BusinessException("Chi ho tro file anh");
            }

            String extension = extensionFromContentType(contentType);
            String assetId = UUID.randomUUID() + "." + extension;
            String safeName = fileName == null || fileName.isBlank() ? "image." + extension : fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
            assets.put(assetId, new CollaboraAsset(assetId, safeName, contentType, content));

            return CollaboraAssetResponse.builder()
                    .assetId(assetId)
                    .fileName(safeName)
                    .url(assetPublicUrl.replaceAll("/+$", "") + "/" + URLEncoder.encode(assetId, StandardCharsets.UTF_8))
                    .build();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Khong the tao anh tam cho Collabora: " + e.getMessage());
        }
    }

    public CollaboraAsset getAsset(String assetId) {
        CollaboraAsset asset = assets.get(assetId);
        if (asset == null) {
            throw new ResourceNotFoundException("Khong tim thay anh Collabora");
        }
        return asset;
    }

    public CollaboraFileSession requireSession(String fileId, String accessToken) {
        if (isInvalidFileId(fileId)) {
            throw new BusinessException("File Collabora khong hop le");
        }
        CollaboraFileSession session = activeSessions.get(sessionKey(fileId, accessToken));
        if (session == null || !session.getAccessToken().equals(accessToken)) {
            throw new CollaboraUnauthorizedException();
        }
        return session;
    }

    public void requireWritable(CollaboraFileSession session) {
        if (!session.isCanWrite()) {
            throw new CollaboraUnauthorizedException();
        }
    }

    public long fileSize(CollaboraFileSession session) {
        try {
            return supabaseStorageService.getFileSize(session.getFileId());
        } catch (Exception e) {
            return 0;
        }
    }

    public byte[] readFile(CollaboraFileSession session) {
        try {
            return supabaseStorageService.downloadFile(session.getFileId());
        } catch (Exception e) {
            throw new BusinessException("Khong the doc file Collabora tu Supabase");
        }
    }

    public void writeFile(CollaboraFileSession session, byte[] content) {
        if (!session.isCanPersist()) {
            return;
        }

        try {
            supabaseStorageService.uploadFile(session.getFileId(), content, contentType(session.getExtension()));
        } catch (Exception e) {
            throw new BusinessException("Khong the luu file Collabora len Supabase");
        }
    }

    private CollaboraMetadata readMetadata(LessonDraft draft) {
        try {
            JsonNode root = objectMapper.readTree(draft.getCanvasJson());
            String fileId = root.path("collaboraFileId").asText("");
            String fileName = root.path("fileName").asText("");
            String extension = root.path("extension").asText("");
            if (fileId.isBlank() || fileName.isBlank() || extension.isBlank()) {
                throw new IllegalArgumentException("Missing Collabora metadata");
            }
            return new CollaboraMetadata(fileId, fileName, extension);
        } catch (Exception e) {
            throw new BusinessException("Du lieu Collabora cua bai giang khong hop le");
        }
    }

    private String resolveActionUrl(String extension, String actionName) {
        String cacheKey = extension + ":" + actionName;
        if (actionUrlCache.containsKey(cacheKey)) {
            return actionUrlCache.get(cacheKey);
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(discoveryUrl))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != HttpStatus.OK.value()) {
                throw new BusinessException("Khong the lay Collabora discovery");
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(response.body().getBytes(StandardCharsets.UTF_8)));
            NodeList appNodes = doc.getElementsByTagName("app");
            for (int i = 0; i < appNodes.getLength(); i++) {
                Element appNode = (Element) appNodes.item(i);
                NodeList actionNodes = appNode.getElementsByTagName("action");
                for (int j = 0; j < actionNodes.getLength(); j++) {
                    Element actionNode = (Element) actionNodes.item(j);
                    String ext = actionNode.getAttribute("ext");
                    String discoveredActionName = actionNode.getAttribute("name");
                    String urlSrc = actionNode.getAttribute("urlsrc");
                    if (discoveredActionName != null && !discoveredActionName.isBlank()
                            && ext != null && !ext.isBlank()
                            && urlSrc != null && !urlSrc.isBlank()) {
                        actionUrlCache.put(ext + ":" + discoveredActionName, urlSrc);
                    }
                }
            }

            String actionUrl = actionUrlCache.get(cacheKey);
            if (actionUrl == null) {
                throw new BusinessException("Collabora khong ho tro " + actionName + " cho dinh dang " + extension);
            }
            return actionUrl;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Khong the ket noi Collabora Online");
        }
    }

    private DraftResponse toResponse(LessonDraft draft) {
        return DraftResponse.builder()
                .id(draft.getId())
                .title(draft.getTitle())
                .subject(draft.getSubject())
                .grade(draft.getGrade())
                .volume(draft.getVolume())
                .book(draft.getBook())
                .type(draft.getType())
                .status(draft.getStatus())
                .canvasJson(draft.getCanvasJson())
                .createdAt(draft.getCreatedAt())
                .updatedAt(draft.getUpdatedAt())
                .build();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeType(String type) {
        String normalized = type == null ? "" : type.trim().toUpperCase();
        if (!ALLOWED_TYPES.contains(normalized)) {
            throw new BusinessException("Collabora chi ho tro COLLABORA_DOCX hoac COLLABORA_PPTX");
        }
        return normalized;
    }

    private String extensionForType(String type) {
        return "COLLABORA_PPTX".equals(type) ? "pptx" : "docx";
    }

    private String extensionFromFileName(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase();
    }

    private String safeFileName(String title, String extension) {
        String base = title == null || title.isBlank() ? "bai-giang-collabora" : title.trim();
        base = base.replaceAll("[\\\\/:*?\"<>|]", "_");
        if (!base.toLowerCase().endsWith("." + extension)) {
            base += "." + extension;
        }
        return base;
    }

    private String stripExtension(String fileName, String extension) {
        String suffix = "." + extension;
        return fileName.toLowerCase().endsWith(suffix)
                ? fileName.substring(0, fileName.length() - suffix.length())
                : fileName;
    }

    private String contentType(String extension) {
        if ("pptx".equals(extension)) {
            return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        }
        return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    }

    private String extensionFromContentType(String contentType) {
        String normalized = contentType == null ? "" : contentType.toLowerCase();
        if (normalized.contains("jpeg") || normalized.contains("jpg")) return "jpg";
        if (normalized.contains("gif")) return "gif";
        if (normalized.contains("webp")) return "webp";
        return "png";
    }

    private URI parseImageSourceUrl(String sourceUrl) {
        URI uri;
        try {
            uri = URI.create(sourceUrl.trim());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("URL anh khong hop le");
        }

        String scheme = uri.getScheme();
        if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
            throw new BusinessException("URL anh phai la http hoac https");
        }
        return uri;
    }

    private String sessionKey(String fileId, String accessToken) {
        return fileId + ":" + accessToken;
    }

    private boolean isInvalidFileId(String fileId) {
        if (fileId == null || fileId.contains("..") || fileId.contains("/") || fileId.contains("\\")) {
            return true;
        }
        int dot = fileId.lastIndexOf('.');
        if (dot < 0 || dot == fileId.length() - 1) {
            return true;
        }
        return !ALLOWED_EXTENSIONS.contains(fileId.substring(dot + 1).toLowerCase());
    }

    private record CollaboraMetadata(String fileId, String fileName, String extension) {
    }

    @Getter
    public static class CollaboraFileSession {
        private final String fileId;
        private final String fileName;
        private final String extension;
        private final Long userId;
        private final Long draftId;
        private final String accessToken;
        private final boolean canWrite;
        private final boolean canPersist;

        public CollaboraFileSession(
                String fileId,
                String fileName,
                String extension,
                Long userId,
                Long draftId,
                String accessToken,
                boolean canWrite,
                boolean canPersist
        ) {
            this.fileId = fileId;
            this.fileName = fileName;
            this.extension = extension;
            this.userId = userId;
            this.draftId = draftId;
            this.accessToken = accessToken;
            this.canWrite = canWrite;
            this.canPersist = canPersist;
        }
    }

    public static class CollaboraUnauthorizedException extends RuntimeException {
    }

    @Getter
    public static class CollaboraAsset {
        private final String assetId;
        private final String fileName;
        private final String contentType;
        private final byte[] content;

        public CollaboraAsset(String assetId, String fileName, String contentType, byte[] content) {
            this.assetId = assetId;
            this.fileName = fileName;
            this.contentType = contentType;
            this.content = content;
        }
    }
}
