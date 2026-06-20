package vn.edu.primary.teacher_support.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.primary.teacher_support.dto.AdminDraftResponse;
import vn.edu.primary.teacher_support.dto.DraftResponse;
import vn.edu.primary.teacher_support.dto.SaveDraftRequest;
import vn.edu.primary.teacher_support.dto.UpdateDraftMetadataRequest;
import vn.edu.primary.teacher_support.entity.LessonDraft;
import vn.edu.primary.teacher_support.entity.enums.LessonDraftStatus;
import vn.edu.primary.teacher_support.exception.ResourceNotFoundException;
import vn.edu.primary.teacher_support.repository.LessonDraftRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonDraftService {

    private final LessonDraftRepository draftRepository;
    private final UserServiceClient userServiceClient;

    public DraftResponse saveDraft(Long userId, SaveDraftRequest request) {
        LessonDraft draft;
        if (request.getDraftId() != null) {
            draft = draftRepository.findByIdAndUserId(request.getDraftId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản nháp"));
            draft.setTitle(request.getTitle());
            draft.setSubject(request.getSubject());
            draft.setGrade(request.getGrade());
            draft.setType(request.getType());
            draft.setCanvasJson(request.getCanvasJson());
        } else {
            draft = LessonDraft.builder()
                    .userId(userId)
                    .title(request.getTitle())
                    .subject(request.getSubject())
                    .grade(request.getGrade())
                    .type(request.getType())
                    .canvasJson(request.getCanvasJson())
                    .build();
        }
        draft = draftRepository.save(draft);
        return toResponse(draft);
    }

    public List<DraftResponse> getDrafts(Long userId) {
        return draftRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    public List<DraftResponse> searchDrafts(Long userId, String title, String subject, String grade) {
        return draftRepository.searchDrafts(userId, title, subject, grade).stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    public DraftResponse getDraft(Long id, Long userId) {
        LessonDraft draft = draftRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản nháp với id: " + id));
        return toResponse(draft);
    }

    public void deleteDraft(Long id, Long userId) {
        LessonDraft draft = draftRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản nháp với id: " + id));
        draftRepository.delete(draft);
    }

    private DraftResponse toResponse(LessonDraft draft) {
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

    private DraftResponse toSummaryResponse(LessonDraft draft) {
        return DraftResponse.builder()
                .id(draft.getId())
                .title(draft.getTitle())
                .subject(draft.getSubject())
                .grade(draft.getGrade())
                .type(draft.getType())
                .status(draft.getStatus())
                .createdAt(draft.getCreatedAt())
                .updatedAt(draft.getUpdatedAt())
                .build();
    }

    public DraftResponse updateMetadata(Long id, Long userId, UpdateDraftMetadataRequest request) {
        LessonDraft draft = draftRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản nháp với id: " + id));
        draft.setTitle(request.getTitle());
        draft.setSubject(request.getSubject());
        draft.setGrade(request.getGrade());
        draft = draftRepository.save(draft);
        return toSummaryResponse(draft);
    }

    public List<AdminDraftResponse> getAllDraftsForAdmin() {
        return draftRepository.findAllByOrderByUpdatedAtDesc().stream()
                .map(this::toAdminResponse)
                .toList();
    }

    public void deleteDraftForAdmin(Long id) {
        LessonDraft draft = draftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài giảng với id: " + id));
        draftRepository.delete(draft);
    }

    private AdminDraftResponse toAdminResponse(LessonDraft draft) {
        String createdByName = userServiceClient.findById(draft.getUserId())
                .map(u -> u.getUsername() != null ? u.getUsername() : u.getEmail())
                .orElse("Unknown");
        return AdminDraftResponse.builder()
                .id(draft.getId())
                .title(draft.getTitle())
                .subject(draft.getSubject())
                .grade(draft.getGrade())
                .type(draft.getType())
                .status(draft.getStatus())
                .createdByName(createdByName)
                .createdAt(draft.getCreatedAt())
                .updatedAt(draft.getUpdatedAt())
                .build();
    }

    public DraftResponse updateStatus(Long id, Long userId, LessonDraftStatus status) {
        LessonDraft draft = draftRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản nháp với id: " + id));
        draft.setStatus(status);
        draft = draftRepository.save(draft);
        return toSummaryResponse(draft);
    }
}
