package vn.edu.primary.teacher_support.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.primary.teacher_support.dto.*;
import vn.edu.primary.teacher_support.entity.LessonDraft;
import vn.edu.primary.teacher_support.entity.LessonPublicRating;
import vn.edu.primary.teacher_support.entity.LessonPublicReport;
import vn.edu.primary.teacher_support.entity.LessonPublicVerificationConfig;
import vn.edu.primary.teacher_support.entity.enums.PublicReportStatus;
import vn.edu.primary.teacher_support.entity.enums.PublicVerificationStatus;
import vn.edu.primary.teacher_support.exception.BusinessException;
import vn.edu.primary.teacher_support.exception.ForbiddenException;
import vn.edu.primary.teacher_support.exception.ResourceNotFoundException;
import vn.edu.primary.teacher_support.repository.LessonDraftRepository;
import vn.edu.primary.teacher_support.repository.LessonPublicRatingRepository;
import vn.edu.primary.teacher_support.repository.LessonPublicReportRepository;
import vn.edu.primary.teacher_support.repository.LessonPublicVerificationConfigRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LessonPublicService {

    private static final List<String> COLLABORA_TYPES = List.of("COLLABORA_DOCX", "COLLABORA_PPTX");

    private final LessonDraftRepository draftRepository;
    private final LessonPublicRatingRepository ratingRepository;
    private final LessonPublicReportRepository reportRepository;
    private final LessonPublicVerificationConfigRepository configRepository;
    private final UserServiceClient userServiceClient;
    private final SupabaseStorageService supabaseStorageService;
    private final NotificationClient notificationClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LessonPublicVerificationConfig getOrCreateConfig() {
        return configRepository.findAll().stream().findFirst().orElseGet(() ->
                configRepository.save(LessonPublicVerificationConfig.builder().build())
        );
    }

    public PublicVerificationConfigDto getConfigDto() {
        return toConfigDto(getOrCreateConfig());
    }

    @Transactional
    public PublicVerificationConfigDto updateConfig(PublicVerificationConfigDto dto) {
        LessonPublicVerificationConfig config = getOrCreateConfig();
        if (dto.getMinCopyCount() != null) config.setMinCopyCount(Math.max(0, dto.getMinCopyCount()));
        if (dto.getMinAverageRating() != null) config.setMinAverageRating(Math.max(0, dto.getMinAverageRating()));
        if (dto.getMinRatingCount() != null) config.setMinRatingCount(Math.max(0, dto.getMinRatingCount()));
        if (dto.getMaxOpenReports() != null) config.setMaxOpenReports(Math.max(0, dto.getMaxOpenReports()));
        if (dto.getMinPublicDays() != null) config.setMinPublicDays(Math.max(0, dto.getMinPublicDays()));
        if (dto.getAutoHideOpenReportThreshold() != null) {
            config.setAutoHideOpenReportThreshold(Math.max(1, dto.getAutoHideOpenReportThreshold()));
        }
        return toConfigDto(configRepository.save(config));
    }

    @Transactional
    public PublicLessonResponse publish(Long draftId, Long ownerUserId) {
        LessonDraft draft = requireOwned(draftId, ownerUserId);
        ensurePublicCounters(draft);
        draft.setIsPublic(true);
        // Always start as unverified; verification is earned later by metrics.
        draft.setPublicVerificationStatus(PublicVerificationStatus.UNVERIFIED);
        if (draft.getPublicPublishedAt() == null) {
            draft.setPublicPublishedAt(LocalDateTime.now());
        }
        draft = draftRepository.save(draft);
        log.info("User {} published draft {} publicly", ownerUserId, draftId);
        return toPublicResponse(draft, ownerUserId, false);
    }

    @Transactional
    public PublicLessonResponse unpublish(Long draftId, Long ownerUserId) {
        LessonDraft draft = requireOwned(draftId, ownerUserId);
        draft.setIsPublic(false);
        draft.setPublicVerificationStatus(PublicVerificationStatus.UNVERIFIED);
        draft = draftRepository.save(draft);
        log.info("User {} unpublished draft {}", ownerUserId, draftId);
        return toPublicResponse(draft, ownerUserId, false);
    }

    public PublicLessonResponse getStatus(Long draftId, Long ownerUserId) {
        LessonDraft draft = requireOwned(draftId, ownerUserId);
        return toPublicResponse(draft, ownerUserId, false);
    }

    public List<PublicLessonResponse> listPublic(
            Long viewerId,
            String subject,
            String grade,
            String type,
            String keyword,
            String verificationStatus
    ) {
        PublicVerificationStatus statusFilter = parseVerification(verificationStatus);
        return draftRepository.searchPublicDrafts(
                        blankToNull(subject),
                        blankToNull(grade),
                        blankToNull(type),
                        blankToNull(keyword),
                        statusFilter
                ).stream()
                .map(d -> toPublicResponse(d, viewerId, false))
                .toList();
    }

    public PublicLessonResponse getPublicLesson(Long draftId, Long viewerId, boolean includeCanvas) {
        LessonDraft draft = requirePublic(draftId);
        return toPublicResponse(draft, viewerId, includeCanvas);
    }

    @Transactional
    public DraftResponse copyToMyLessons(Long draftId, Long userId) {
        LessonDraft original = requirePublic(draftId);
        if (original.getUserId().equals(userId)) {
            throw new BusinessException("Đây là bài giảng của bạn — không cần sao chép");
        }

        String canvasJson = original.getCanvasJson();
        if (isCollaboraType(original.getType())) {
            canvasJson = cloneCollaboraFile(original, userId);
        }

        LessonDraft copy = LessonDraft.builder()
                .userId(userId)
                .title(original.getTitle() + " (Bản sao)")
                .subject(original.getSubject())
                .grade(original.getGrade())
                .volume(original.getVolume())
                .book(original.getBook())
                .type(original.getType())
                .canvasJson(canvasJson)
                .isPublic(false)
                .publicVerificationStatus(PublicVerificationStatus.UNVERIFIED)
                .publicCopyCount(0)
                .publicAverageRating(0.0)
                .publicRatingCount(0)
                .publicOpenReportCount(0)
                .build();
        copy = draftRepository.save(copy);

        original.setPublicCopyCount(nvl(original.getPublicCopyCount()) + 1);
        reevaluateVerification(original);
        draftRepository.save(original);

        log.info("User {} copied public draft {} -> {}", userId, draftId, copy.getId());
        return DraftResponse.builder()
                .id(copy.getId())
                .title(copy.getTitle())
                .subject(copy.getSubject())
                .grade(copy.getGrade())
                .volume(copy.getVolume())
                .book(copy.getBook())
                .type(copy.getType())
                .status(copy.getStatus())
                .canvasJson(copy.getCanvasJson())
                .isPublic(false)
                .publicVerificationStatus(PublicVerificationStatus.UNVERIFIED)
                .publicCopyCount(0)
                .publicAverageRating(0.0)
                .publicRatingCount(0)
                .publicOpenReportCount(0)
                .createdAt(copy.getCreatedAt())
                .updatedAt(copy.getUpdatedAt())
                .build();
    }

    @Transactional
    public PublicLessonResponse rate(Long draftId, Long userId, int stars) {
        if (stars < 1 || stars > 5) {
            throw new BusinessException("Điểm đánh giá phải từ 1 đến 5 sao");
        }
        LessonDraft draft = requirePublic(draftId);
        if (draft.getUserId().equals(userId)) {
            throw new BusinessException("Không thể tự đánh giá bài giảng của mình");
        }

        Optional<LessonPublicRating> existing = ratingRepository.findByDraftIdAndUserId(draftId, userId);
        if (existing.isPresent()) {
            LessonPublicRating rating = existing.get();
            rating.setStars(stars);
            ratingRepository.save(rating);
        } else {
            ratingRepository.save(LessonPublicRating.builder()
                    .draftId(draftId)
                    .userId(userId)
                    .stars(stars)
                    .build());
        }

        refreshRatingCache(draft);
        reevaluateVerification(draft);
        draft = draftRepository.save(draft);
        PublicLessonResponse response = toPublicResponse(draft, userId, false);
        response.setMyRating(stars);
        return response;
    }

    public Map<String, Object> getMyRating(Long draftId, Long userId) {
        requirePublic(draftId);
        return ratingRepository.findByDraftIdAndUserId(draftId, userId)
                .map(r -> Map.<String, Object>of("stars", r.getStars()))
                .orElse(Map.of());
    }

    @Transactional
    public PublicReportResponse report(Long draftId, Long reporterId, String reason, String detail) {
        LessonDraft draft = requirePublic(draftId);
        if (draft.getUserId().equals(reporterId)) {
            throw new BusinessException("Không thể báo cáo bài giảng của mình");
        }
        if (reason == null || reason.isBlank()) {
            throw new BusinessException("Vui lòng chọn lý do báo cáo");
        }
        if (reportRepository.findByDraftIdAndReporterIdAndStatus(draftId, reporterId, PublicReportStatus.OPEN).isPresent()) {
            throw new BusinessException("Bạn đã có báo cáo đang mở cho bài giảng này");
        }

        LessonPublicReport report = reportRepository.save(LessonPublicReport.builder()
                .draftId(draftId)
                .reporterId(reporterId)
                .reason(reason.trim())
                .detail(detail == null ? null : detail.trim())
                .status(PublicReportStatus.OPEN)
                .build());

        int openCount = (int) reportRepository.countByDraftIdAndStatus(draftId, PublicReportStatus.OPEN);
        draft.setPublicOpenReportCount(openCount);
        LessonPublicVerificationConfig config = getOrCreateConfig();
        if (openCount >= nvl(config.getAutoHideOpenReportThreshold(), 3)) {
            autoHideForReports(draft, config);
        } else {
            reevaluateVerification(draft);
            draftRepository.save(draft);
        }

        notifyOwnerReported(draft);
        return toReportResponse(report, draft);
    }

    public List<PublicReportResponse> listAdminReports(String status) {
        List<LessonPublicReport> reports;
        if (status == null || status.isBlank() || "all".equalsIgnoreCase(status)) {
            reports = reportRepository.findAllByOrderByCreatedAtDesc();
        } else {
            reports = reportRepository.findByStatusOrderByCreatedAtDesc(PublicReportStatus.valueOf(status.toUpperCase()));
        }
        return reports.stream()
                .map(r -> {
                    LessonDraft draft = draftRepository.findById(r.getDraftId()).orElse(null);
                    return toReportResponse(r, draft);
                })
                .toList();
    }

    @Transactional
    public PublicReportResponse resolveReport(Long reportId, PublicReportStatus status, String adminNote) {
        if (status != PublicReportStatus.RESOLVED && status != PublicReportStatus.DISMISSED) {
            throw new BusinessException("Trạng thái xử lý không hợp lệ");
        }
        LessonPublicReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy báo cáo"));
        report.setStatus(status);
        report.setAdminNote(adminNote);
        report.setResolvedAt(LocalDateTime.now());
        report = reportRepository.save(report);

        draftRepository.findById(report.getDraftId()).ifPresent(draft -> {
            draft.setPublicOpenReportCount((int) reportRepository.countByDraftIdAndStatus(draft.getId(), PublicReportStatus.OPEN));
            reevaluateVerification(draft);
            draftRepository.save(draft);
        });

        LessonDraft draft = draftRepository.findById(report.getDraftId()).orElse(null);
        return toReportResponse(report, draft);
    }

    public List<PublicLessonResponse> listAdminLessons(Boolean isPublic, String verificationStatus) {
        PublicVerificationStatus statusFilter = parseVerification(verificationStatus);
        // Only drafts that were ever published publicly (or currently public).
        return draftRepository.findAllByOrderByUpdatedAtDesc().stream()
                .filter(d -> Boolean.TRUE.equals(d.getIsPublic()) || d.getPublicPublishedAt() != null)
                .filter(d -> isPublic == null || Boolean.TRUE.equals(d.getIsPublic()) == isPublic)
                .filter(d -> statusFilter == null || statusFilter == effectiveVerification(d))
                .map(d -> toPublicResponse(d, null, false))
                .toList();
    }

    @Transactional
    public PublicLessonResponse adminUnpublish(Long draftId, String reason) {
        LessonDraft draft = draftRepository.findById(draftId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài giảng"));
        draft.setIsPublic(false);
        draft.setPublicVerificationStatus(PublicVerificationStatus.UNVERIFIED);
        draft = draftRepository.save(draft);
        log.info("Admin unpublished public draft {} reason={}", draftId, reason);
        return toPublicResponse(draft, null, false);
    }

    @Transactional
    public PublicLessonResponse reevaluate(Long draftId) {
        LessonDraft draft = draftRepository.findById(draftId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài giảng"));
        refreshRatingCache(draft);
        draft.setPublicOpenReportCount((int) reportRepository.countByDraftIdAndStatus(draftId, PublicReportStatus.OPEN));
        List<String> missing = collectMissingConditions(draft);
        reevaluateVerification(draft);
        draft = draftRepository.save(draft);
        PublicLessonResponse response = toPublicResponse(draft, null, false);
        if (response.getPublicVerificationStatus() != PublicVerificationStatus.VERIFIED) {
            response.setMissingConditions(missing);
        } else {
            response.setMissingConditions(List.of());
        }
        return response;
    }

    /** Build list of unmet auto-verify rules for admin feedback. */
    public List<String> collectMissingConditions(LessonDraft draft) {
        List<String> missing = new java.util.ArrayList<>();
        if (draft == null) {
            missing.add("Không tìm thấy bài giảng");
            return missing;
        }
        if (!Boolean.TRUE.equals(draft.getIsPublic())) {
            missing.add("Bài giảng chưa đang công khai");
            return missing;
        }

        LessonPublicVerificationConfig config = getOrCreateConfig();
        int minCopy = nvl(config.getMinCopyCount(), 5);
        double minAvg = nvl(config.getMinAverageRating(), 4.0);
        int minRatings = nvl(config.getMinRatingCount(), 3);
        int maxOpen = nvl(config.getMaxOpenReports(), 0);
        int minDays = nvl(config.getMinPublicDays(), 3);

        int copies = nvl(draft.getPublicCopyCount());
        if (copies < minCopy) {
            int need = minCopy - copies;
            missing.add("cần thêm " + need + " lượt sao chép (hiện " + copies + "/" + minCopy + ")");
        }

        int ratingCount = nvl(draft.getPublicRatingCount());
        if (ratingCount < minRatings) {
            int need = minRatings - ratingCount;
            missing.add("cần thêm " + need + " lượt đánh giá (hiện " + ratingCount + "/" + minRatings + ")");
        }

        double avg = nvl(draft.getPublicAverageRating());
        if (ratingCount > 0 && avg < minAvg) {
            missing.add("điểm TB cần ≥ " + minAvg + " sao (hiện " + String.format("%.1f", avg) + ")");
        } else if (ratingCount == 0 && minRatings > 0) {
            // already covered by rating count; skip duplicate avg message when no ratings
        }

        int openReports = nvl(draft.getPublicOpenReportCount());
        if (openReports > maxOpen) {
            missing.add("còn " + openReports + " báo cáo đang mở (tối đa cho phép: " + maxOpen + ")");
        }

        if (draft.getPublicPublishedAt() == null) {
            missing.add("chưa có ngày bắt đầu công khai");
        } else {
            long days = ChronoUnit.DAYS.between(draft.getPublicPublishedAt(), LocalDateTime.now());
            if (days < minDays) {
                long need = minDays - days;
                missing.add("cần thêm " + need + " ngày public (hiện " + days + "/" + minDays + " ngày)");
            }
        }

        return missing;
    }

    /** Call when owner edits content of a verified public lesson. */
    @Transactional
    public void onOwnerContentEdited(LessonDraft draft) {
        if (draft == null) return;
        if (Boolean.TRUE.equals(draft.getIsPublic())
                && draft.getPublicVerificationStatus() == PublicVerificationStatus.VERIFIED) {
            draft.setPublicVerificationStatus(PublicVerificationStatus.UNVERIFIED);
            draftRepository.save(draft);
            log.info("Reset verification to UNVERIFIED after content edit on draft {}", draft.getId());
        }
    }

    public void reevaluateVerification(LessonDraft draft) {
        if (draft == null || !Boolean.TRUE.equals(draft.getIsPublic())) {
            if (draft != null) {
                draft.setPublicVerificationStatus(PublicVerificationStatus.UNVERIFIED);
            }
            return;
        }
        LessonPublicVerificationConfig config = getOrCreateConfig();
        boolean ok = nvl(draft.getPublicCopyCount()) >= nvl(config.getMinCopyCount(), 5)
                && nvl(draft.getPublicAverageRating()) >= nvl(config.getMinAverageRating(), 4.0)
                && nvl(draft.getPublicRatingCount()) >= nvl(config.getMinRatingCount(), 3)
                && nvl(draft.getPublicOpenReportCount()) <= nvl(config.getMaxOpenReports(), 0)
                && draft.getPublicPublishedAt() != null
                && ChronoUnit.DAYS.between(draft.getPublicPublishedAt(), LocalDateTime.now())
                >= nvl(config.getMinPublicDays(), 3);

        draft.setPublicVerificationStatus(ok
                ? PublicVerificationStatus.VERIFIED
                : PublicVerificationStatus.UNVERIFIED);
    }

    private void autoHideForReports(LessonDraft draft, LessonPublicVerificationConfig config) {
        draft.setIsPublic(false);
        draft.setPublicVerificationStatus(PublicVerificationStatus.UNVERIFIED);
        draftRepository.save(draft);
        log.warn("Auto-hid public draft {} due to open reports >= {}", draft.getId(), config.getAutoHideOpenReportThreshold());

        notificationClient.notifyUser(
                draft.getUserId(),
                null,
                "Hệ thống",
                "LESSON_PUBLIC_AUTO_HIDDEN",
                "Bài giảng công khai đã bị ẩn",
                "Bài \"" + draft.getTitle() + "\" đã bị ẩn khỏi danh mục công khai do có từ "
                        + config.getAutoHideOpenReportThreshold() + " báo cáo đang mở.",
                "/lessons",
                "LESSON",
                draft.getId()
        );
    }

    private void notifyOwnerReported(LessonDraft draft) {
        notificationClient.notifyUser(
                draft.getUserId(),
                null,
                "Hệ thống",
                "LESSON_PUBLIC_REPORTED",
                "Bài giảng công khai bị báo cáo",
                "Bài \"" + draft.getTitle() + "\" nhận một báo cáo mới từ giáo viên khác.",
                "/lessons",
                "LESSON",
                draft.getId()
        );
    }

    private PublicVerificationStatus effectiveVerification(LessonDraft draft) {
        return draft.getPublicVerificationStatus() != null
                ? draft.getPublicVerificationStatus()
                : PublicVerificationStatus.UNVERIFIED;
    }

    private void refreshRatingCache(LessonDraft draft) {
        long count = ratingRepository.countByDraftId(draft.getId());
        Double avg = ratingRepository.averageStarsByDraftId(draft.getId());
        draft.setPublicRatingCount((int) count);
        draft.setPublicAverageRating(avg == null ? 0.0 : Math.round(avg * 10.0) / 10.0);
    }

    private String cloneCollaboraFile(LessonDraft original, Long userId) {
        try {
            JsonNode meta = objectMapper.readTree(original.getCanvasJson() == null ? "{}" : original.getCanvasJson());
            String sourceFileId = text(meta, "collaboraFileId");
            if (sourceFileId == null || sourceFileId.isBlank()) {
                throw new BusinessException("Bài giảng Collabora thiếu file gốc");
            }
            String extension = text(meta, "extension");
            if (extension == null || extension.isBlank()) {
                extension = sourceFileId.contains(".") ? sourceFileId.substring(sourceFileId.lastIndexOf('.') + 1) : "docx";
            }
            String fileName = text(meta, "fileName");
            if (fileName == null || fileName.isBlank()) {
                fileName = original.getTitle() + "." + extension;
            }
            String newFileId = "lesson-" + userId + "-" + UUID.randomUUID() + "." + extension;
            byte[] content = supabaseStorageService.downloadFile(sourceFileId);
            String contentType = "pptx".equalsIgnoreCase(extension)
                    ? "application/vnd.openxmlformats-officedocument.presentationml.presentation"
                    : "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            supabaseStorageService.uploadFile(newFileId, content, contentType);

            ObjectNode node = objectMapper.createObjectNode();
            node.put("collaboraFileId", newFileId);
            node.put("fileName", fileName);
            node.put("extension", extension);
            node.put("copiedFromPublicDraftId", original.getId());
            return objectMapper.writeValueAsString(node);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Không thể sao chép file Collabora: " + e.getMessage());
        }
    }

    private LessonDraft requireOwned(Long draftId, Long ownerUserId) {
        return draftRepository.findByIdAndUserId(draftId, ownerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài giảng hoặc bạn không phải chủ sở hữu"));
    }

    private LessonDraft requirePublic(Long draftId) {
        LessonDraft draft = draftRepository.findById(draftId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài giảng"));
        if (!Boolean.TRUE.equals(draft.getIsPublic())) {
            throw new ForbiddenException("Bài giảng này không còn công khai");
        }
        return draft;
    }

    private PublicLessonResponse toPublicResponse(LessonDraft draft, Long viewerId, boolean includeCanvas) {
        ensurePublicCounters(draft);
        UserDto owner = userServiceClient.findById(draft.getUserId()).orElse(null);
        Integer myRating = null;
        if (viewerId != null) {
            myRating = ratingRepository.findByDraftIdAndUserId(draft.getId(), viewerId)
                    .map(LessonPublicRating::getStars)
                    .orElse(null);
        }
        boolean isOwner = viewerId != null && viewerId.equals(draft.getUserId());
        return PublicLessonResponse.builder()
                .id(draft.getId())
                .title(draft.getTitle())
                .subject(draft.getSubject())
                .grade(draft.getGrade())
                .volume(draft.getVolume())
                .book(draft.getBook())
                .type(draft.getType())
                .status(draft.getStatus())
                .canvasJson(includeCanvas ? draft.getCanvasJson() : null)
                .ownerId(draft.getUserId())
                .ownerName(owner != null ? (owner.getUsername() != null ? owner.getUsername() : owner.getEmail()) : "Giáo viên")
                .ownerEmail(owner != null ? owner.getEmail() : null)
                .isOwner(isOwner)
                .isPublic(Boolean.TRUE.equals(draft.getIsPublic()))
                .publicVerificationStatus(draft.getPublicVerificationStatus() != null
                        ? draft.getPublicVerificationStatus()
                        : PublicVerificationStatus.UNVERIFIED)
                .publicPublishedAt(draft.getPublicPublishedAt())
                .publicCopyCount(nvl(draft.getPublicCopyCount()))
                .publicAverageRating(nvl(draft.getPublicAverageRating()))
                .publicRatingCount(nvl(draft.getPublicRatingCount()))
                .publicOpenReportCount(nvl(draft.getPublicOpenReportCount()))
                .myRating(myRating)
                .createdAt(draft.getCreatedAt())
                .updatedAt(draft.getUpdatedAt())
                .build();
    }

    private PublicReportResponse toReportResponse(LessonPublicReport report, LessonDraft draft) {
        UserDto reporter = userServiceClient.findById(report.getReporterId()).orElse(null);
        UserDto owner = draft != null ? userServiceClient.findById(draft.getUserId()).orElse(null) : null;
        return PublicReportResponse.builder()
                .id(report.getId())
                .draftId(report.getDraftId())
                .lessonTitle(draft != null ? draft.getTitle() : null)
                .lessonType(draft != null ? draft.getType() : null)
                .ownerName(owner != null ? (owner.getUsername() != null ? owner.getUsername() : owner.getEmail()) : null)
                .reporterId(report.getReporterId())
                .reporterName(reporter != null ? (reporter.getUsername() != null ? reporter.getUsername() : reporter.getEmail()) : null)
                .reporterEmail(reporter != null ? reporter.getEmail() : null)
                .reason(report.getReason())
                .detail(report.getDetail())
                .status(report.getStatus())
                .adminNote(report.getAdminNote())
                .createdAt(report.getCreatedAt())
                .resolvedAt(report.getResolvedAt())
                .build();
    }

    private PublicVerificationConfigDto toConfigDto(LessonPublicVerificationConfig c) {
        return PublicVerificationConfigDto.builder()
                .minCopyCount(c.getMinCopyCount())
                .minAverageRating(c.getMinAverageRating())
                .minRatingCount(c.getMinRatingCount())
                .maxOpenReports(c.getMaxOpenReports())
                .minPublicDays(c.getMinPublicDays())
                .autoHideOpenReportThreshold(c.getAutoHideOpenReportThreshold())
                .build();
    }

    private void ensurePublicCounters(LessonDraft draft) {
        if (draft.getIsPublic() == null) draft.setIsPublic(false);
        if (draft.getPublicVerificationStatus() == null) draft.setPublicVerificationStatus(PublicVerificationStatus.UNVERIFIED);
        if (draft.getPublicCopyCount() == null) draft.setPublicCopyCount(0);
        if (draft.getPublicAverageRating() == null) draft.setPublicAverageRating(0.0);
        if (draft.getPublicRatingCount() == null) draft.setPublicRatingCount(0);
        if (draft.getPublicOpenReportCount() == null) draft.setPublicOpenReportCount(0);
    }

    private PublicVerificationStatus parseVerification(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return PublicVerificationStatus.valueOf(value.trim().toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isCollaboraType(String type) {
        return type != null && COLLABORA_TYPES.contains(type);
    }

    private String blankToNull(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }

    private String text(JsonNode node, String field) {
        JsonNode child = node.get(field);
        return child == null || child.isNull() ? null : child.asText();
    }

    private int nvl(Integer v) {
        return v == null ? 0 : v;
    }

    private int nvl(Integer v, int d) {
        return v == null ? d : v;
    }

    private double nvl(Double v) {
        return v == null ? 0.0 : v;
    }

    private double nvl(Double v, double d) {
        return v == null ? d : v;
    }
}
