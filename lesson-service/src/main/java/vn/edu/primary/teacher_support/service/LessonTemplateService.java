package vn.edu.primary.teacher_support.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.primary.teacher_support.dto.DraftResponse;
import vn.edu.primary.teacher_support.dto.LessonTemplateResponse;
import vn.edu.primary.teacher_support.dto.UpdateLessonTemplateRequest;
import vn.edu.primary.teacher_support.entity.LessonDraft;
import vn.edu.primary.teacher_support.entity.LessonTemplate;
import vn.edu.primary.teacher_support.entity.enums.LessonTemplateStatus;
import vn.edu.primary.teacher_support.exception.BusinessException;
import vn.edu.primary.teacher_support.exception.ResourceNotFoundException;
import vn.edu.primary.teacher_support.repository.LessonDraftRepository;
import vn.edu.primary.teacher_support.repository.LessonTemplateRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LessonTemplateService {

    private static final List<String> ALLOWED_EXTENSIONS = List.of("docx", "pptx");
    private static final List<String> ALLOWED_TYPES = List.of("COLLABORA_DOCX", "COLLABORA_PPTX");

    private final LessonTemplateRepository templateRepository;
    private final LessonDraftRepository draftRepository;
    private final SupabaseStorageService supabaseStorageService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<LessonTemplateResponse> getActiveTemplates(String subject, String grade, String type) {
        String normalizedType = normalizeNullableType(type);
        return templateRepository.findVisibleTemplates(
                        LessonTemplateStatus.ACTIVE,
                        blankToNull(subject),
                        blankToNull(grade),
                        normalizedType
                ).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<LessonTemplateResponse> getAdminTemplates() {
        return templateRepository.findAllByOrderByUpdatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    public LessonTemplateResponse uploadTemplate(
            Long adminUserId,
            MultipartFile file,
            String title,
            String description,
            String subject,
            String grade,
            LessonTemplateStatus status
    ) {
        validateRequired(title, "Ten mau khong duoc de trong");
        validateRequired(subject, "Mon hoc khong duoc de trong");
        validateRequired(grade, "Lop khong duoc de trong");
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Vui long chon file mau");
        }

        String extension = extensionFromFileName(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("Mau bai giang chi ho tro file .docx hoac .pptx");
        }

        String type = typeForExtension(extension);
        String safeTitle = title.trim();
        String fileName = safeFileName(safeTitle, extension);
        String fileId = "template-" + adminUserId + "-" + UUID.randomUUID() + "." + extension;

        try {
            supabaseStorageService.uploadFile(fileId, file.getBytes(), contentType(extension));
            LessonTemplate template = LessonTemplate.builder()
                    .title(safeTitle)
                    .description(blankToNull(description))
                    .subject(subject.trim())
                    .grade(grade.trim())
                    .type(type)
                    .fileId(fileId)
                    .fileName(fileName)
                    .extension(extension)
                    .status(status == null ? LessonTemplateStatus.ACTIVE : status)
                    .createdByUserId(adminUserId)
                    .build();
            return toResponse(templateRepository.save(template));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Khong the tai mau bai giang len storage: " + e.getMessage());
        }
    }

    public LessonTemplateResponse updateTemplate(Long id, UpdateLessonTemplateRequest request) {
        LessonTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay mau bai giang"));

        if (request.getTitle() != null) {
            validateRequired(request.getTitle(), "Ten mau khong duoc de trong");
            template.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            template.setDescription(blankToNull(request.getDescription()));
        }
        if (request.getSubject() != null) {
            validateRequired(request.getSubject(), "Mon hoc khong duoc de trong");
            template.setSubject(request.getSubject().trim());
        }
        if (request.getGrade() != null) {
            validateRequired(request.getGrade(), "Lop khong duoc de trong");
            template.setGrade(request.getGrade().trim());
        }
        if (request.getStatus() != null) {
            template.setStatus(request.getStatus());
        }
        return toResponse(templateRepository.save(template));
    }

    public void deleteTemplate(Long id) {
        LessonTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay mau bai giang"));
        templateRepository.delete(template);
    }

    public DraftResponse useTemplate(Long userId, Long templateId) {
        LessonTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay mau bai giang"));
        if (template.getStatus() != LessonTemplateStatus.ACTIVE) {
            throw new BusinessException("Mau bai giang nay dang tam an");
        }
        if (!ALLOWED_TYPES.contains(template.getType()) || !ALLOWED_EXTENSIONS.contains(template.getExtension())) {
            throw new BusinessException("Mau bai giang khong hop le");
        }

        String draftFileId = "lesson-" + userId + "-" + UUID.randomUUID() + "." + template.getExtension();
        String draftFileName = safeFileName(template.getTitle(), template.getExtension());

        try {
            byte[] content = supabaseStorageService.downloadFile(template.getFileId());
            supabaseStorageService.uploadFile(draftFileId, content, contentType(template.getExtension()));

            LessonDraft draft = LessonDraft.builder()
                    .userId(userId)
                    .title(stripExtension(draftFileName, template.getExtension()))
                    .subject(template.getSubject())
                    .grade(template.getGrade())
                    .type(template.getType())
                    .canvasJson(objectMapper.writeValueAsString(Map.of(
                            "collaboraFileId", draftFileId,
                            "fileName", draftFileName,
                            "extension", template.getExtension(),
                            "templateId", template.getId()
                    )))
                    .build();
            return toDraftResponse(draftRepository.save(draft));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Khong the tao bai giang tu mau: " + e.getMessage());
        }
    }

    private LessonTemplateResponse toResponse(LessonTemplate template) {
        return LessonTemplateResponse.builder()
                .id(template.getId())
                .title(template.getTitle())
                .description(template.getDescription())
                .subject(template.getSubject())
                .grade(template.getGrade())
                .type(template.getType())
                .fileName(template.getFileName())
                .extension(template.getExtension())
                .status(template.getStatus())
                .createdByUserId(template.getCreatedByUserId())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }

    private DraftResponse toDraftResponse(LessonDraft draft) {
        return DraftResponse.builder()
                .id(draft.getId())
                .title(draft.getTitle())
                .subject(draft.getSubject())
                .grade(draft.getGrade())
                .type(draft.getType())
                .status(draft.getStatus())
                .canvasJson(draft.getCanvasJson())
                .createdAt(draft.getCreatedAt())
                .updatedAt(draft.getUpdatedAt())
                .build();
    }

    private void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(message);
        }
    }

    private String normalizeNullableType(String type) {
        String normalized = blankToNull(type);
        if (normalized == null) return null;
        normalized = normalized.toUpperCase();
        if (!ALLOWED_TYPES.contains(normalized)) {
            throw new BusinessException("Loai mau bai giang khong hop le");
        }
        return normalized;
    }

    private String typeForExtension(String extension) {
        return "pptx".equals(extension) ? "COLLABORA_PPTX" : "COLLABORA_DOCX";
    }

    private String extensionFromFileName(String fileName) {
        if (fileName == null) return "";
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) return "";
        return fileName.substring(dot + 1).toLowerCase();
    }

    private String safeFileName(String title, String extension) {
        String base = title == null || title.isBlank() ? "mau-bai-giang" : title.trim();
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

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
