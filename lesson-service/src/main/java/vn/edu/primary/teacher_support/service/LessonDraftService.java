package vn.edu.primary.teacher_support.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.primary.teacher_support.dto.DraftResponse;
import vn.edu.primary.teacher_support.dto.SaveDraftRequest;
import vn.edu.primary.teacher_support.entity.LessonDraft;
import vn.edu.primary.teacher_support.exception.ResourceNotFoundException;
import vn.edu.primary.teacher_support.repository.LessonDraftRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonDraftService {

    private final LessonDraftRepository draftRepository;

    public DraftResponse saveDraft(Long userId, SaveDraftRequest request) {
        LessonDraft draft;
        if (request.getDraftId() != null) {
            draft = draftRepository.findByIdAndUserId(request.getDraftId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản nháp"));
            draft.setTitle(request.getTitle());
            draft.setType(request.getType());
            draft.setCanvasJson(request.getCanvasJson());
        } else {
            draft = LessonDraft.builder()
                    .userId(userId)
                    .title(request.getTitle())
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
                .type(draft.getType())
                .canvasJson(draft.getCanvasJson())
                .createdAt(draft.getCreatedAt())
                .updatedAt(draft.getUpdatedAt())
                .build();
    }

    private DraftResponse toSummaryResponse(LessonDraft draft) {
        return DraftResponse.builder()
                .id(draft.getId())
                .title(draft.getTitle())
                .type(draft.getType())
                .createdAt(draft.getCreatedAt())
                .updatedAt(draft.getUpdatedAt())
                .build();
    }
}
