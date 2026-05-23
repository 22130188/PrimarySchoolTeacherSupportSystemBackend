package vn.edu.primary.teacher_support.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.primary.teacher_support.dto.ShareResponse;
import vn.edu.primary.teacher_support.dto.SharedDraftResponse;
import vn.edu.primary.teacher_support.dto.UserDto;
import vn.edu.primary.teacher_support.entity.LessonDraft;
import vn.edu.primary.teacher_support.entity.LessonShare;
import vn.edu.primary.teacher_support.entity.enums.SharePermission;
import vn.edu.primary.teacher_support.exception.BusinessException;
import vn.edu.primary.teacher_support.exception.ForbiddenException;
import vn.edu.primary.teacher_support.exception.ResourceNotFoundException;
import vn.edu.primary.teacher_support.repository.LessonDraftRepository;
import vn.edu.primary.teacher_support.repository.LessonShareRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LessonShareService {

    private final LessonShareRepository shareRepository;
    private final LessonDraftRepository draftRepository;
    private final UserServiceClient userServiceClient;

    @Transactional
    public ShareResponse shareDraft(Long draftId, Long ownerUserId, String email, SharePermission permission) {
        LessonDraft draft = draftRepository.findByIdAndUserId(draftId, ownerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài giảng hoặc bạn không phải chủ sở hữu"));

        UserDto targetUser = userServiceClient.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giáo viên với email: " + email));

        if (!"TEACHER".equalsIgnoreCase(targetUser.getRole())) {
            throw new BusinessException("Chỉ có thể chia sẻ bài giảng cho tài khoản giáo viên");
        }

        if (targetUser.getId().equals(ownerUserId)) {
            throw new BusinessException("Không thể chia sẻ bài giảng cho chính mình");
        }

        Optional<LessonShare> existingShare = shareRepository.findByDraftIdAndSharedWithUserId(draftId, targetUser.getId());
        LessonShare share;

        if (existingShare.isPresent()) {
            share = existingShare.get();
            share.setPermission(permission);
            share = shareRepository.save(share);
            log.info("Updated share permission for draft {} to user {} -> {}", draftId, targetUser.getId(), permission);
        } else {
            share = LessonShare.builder()
                    .draftId(draftId)
                    .ownerUserId(ownerUserId)
                    .sharedWithUserId(targetUser.getId())
                    .permission(permission)
                    .build();
            share = shareRepository.save(share);
            log.info("Shared draft {} with user {} permission {}", draftId, targetUser.getId(), permission);
        }

        return toShareResponse(share, targetUser);
    }

    public List<ShareResponse> getSharesForDraft(Long draftId, Long ownerUserId) {
        draftRepository.findByIdAndUserId(draftId, ownerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài giảng hoặc bạn không phải chủ sở hữu"));

        return shareRepository.findByDraftIdOrderByCreatedAtDesc(draftId).stream()
                .map(share -> {
                    UserDto user = userServiceClient.findById(share.getSharedWithUserId()).orElse(null);
                    return toShareResponse(share, user);
                })
                .toList();
    }

    @Transactional
    public ShareResponse updatePermission(Long draftId, Long targetUserId, Long ownerUserId, SharePermission permission) {
        draftRepository.findByIdAndUserId(draftId, ownerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài giảng hoặc bạn không phải chủ sở hữu"));

        LessonShare share = shareRepository.findByDraftIdAndSharedWithUserId(draftId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chia sẻ này"));

        share.setPermission(permission);
        share = shareRepository.save(share);

        UserDto user = userServiceClient.findById(targetUserId).orElse(null);
        log.info("Updated share permission for draft {} user {} -> {}", draftId, targetUserId, permission);
        return toShareResponse(share, user);
    }

    @Transactional
    public void revokeShare(Long draftId, Long targetUserId, Long ownerUserId) {
        draftRepository.findByIdAndUserId(draftId, ownerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài giảng hoặc bạn không phải chủ sở hữu"));

        LessonShare share = shareRepository.findByDraftIdAndSharedWithUserId(draftId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chia sẻ này"));

        shareRepository.delete(share);
        log.info("Revoked share for draft {} from user {}", draftId, targetUserId);
    }

    public List<SharedDraftResponse> getSharedWithMe(Long userId) {
        List<LessonShare> shares = shareRepository.findBySharedWithUserIdOrderByCreatedAtDesc(userId);

        return shares.stream()
                .map(share -> {
                    Optional<LessonDraft> draftOpt = draftRepository.findById(share.getDraftId());
                    if (draftOpt.isEmpty()) return null;

                    LessonDraft draft = draftOpt.get();
                    UserDto owner = userServiceClient.findById(share.getOwnerUserId()).orElse(null);

                    return SharedDraftResponse.builder()
                            .id(draft.getId())
                            .title(draft.getTitle())
                            .subject(draft.getSubject())
                            .grade(draft.getGrade())
                            .type(draft.getType())
                            .status(draft.getStatus())
                            .permission(share.getPermission())
                            .ownerName(owner != null ? (owner.getFullName() != null ? owner.getFullName() : owner.getEmail()) : "Unknown")
                            .ownerEmail(owner != null ? owner.getEmail() : "")
                            .createdAt(draft.getCreatedAt())
                            .updatedAt(draft.getUpdatedAt())
                            .build();
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    public SharedDraftResponse getSharedDraft(Long draftId, Long userId) {
        LessonShare share = shareRepository.findByDraftIdAndSharedWithUserId(draftId, userId)
                .orElseThrow(() -> new ForbiddenException("Bạn không có quyền xem bài giảng này"));

        LessonDraft draft = draftRepository.findById(draftId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài giảng"));

        UserDto owner = userServiceClient.findById(share.getOwnerUserId()).orElse(null);

        return SharedDraftResponse.builder()
                .id(draft.getId())
                .title(draft.getTitle())
                .subject(draft.getSubject())
                .grade(draft.getGrade())
                .type(draft.getType())
                .status(draft.getStatus())
                .canvasJson(draft.getCanvasJson())
                .permission(share.getPermission())
                .ownerName(owner != null ? (owner.getFullName() != null ? owner.getFullName() : owner.getEmail()) : "Unknown")
                .ownerEmail(owner != null ? owner.getEmail() : "")
                .createdAt(draft.getCreatedAt())
                .updatedAt(draft.getUpdatedAt())
                .build();
    }

    @Transactional
    public Long duplicateSharedDraft(Long draftId, Long userId) {
        LessonShare share = shareRepository.findByDraftIdAndSharedWithUserId(draftId, userId)
                .orElseThrow(() -> new ForbiddenException("Bạn không có quyền truy cập bài giảng này"));

        if (share.getPermission() != SharePermission.COPY) {
            throw new ForbiddenException("Bạn không có quyền tạo bản sao bài giảng này");
        }

        LessonDraft original = draftRepository.findById(draftId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài giảng gốc"));

        LessonDraft copy = LessonDraft.builder()
                .userId(userId)
                .title(original.getTitle() + " (Bản sao)")
                .subject(original.getSubject())
                .grade(original.getGrade())
                .type(original.getType())
                .canvasJson(original.getCanvasJson())
                .build();

        copy = draftRepository.save(copy);
        log.info("User {} duplicated draft {} -> new draft {}", userId, draftId, copy.getId());
        return copy.getId();
    }

    private ShareResponse toShareResponse(LessonShare share, UserDto user) {
        return ShareResponse.builder()
                .id(share.getId())
                .draftId(share.getDraftId())
                .sharedWithUserId(share.getSharedWithUserId())
                .sharedWithEmail(user != null ? user.getEmail() : "Unknown")
                .sharedWithName(user != null ? (user.getFullName() != null ? user.getFullName() : user.getEmail()) : "Unknown")
                .permission(share.getPermission())
                .createdAt(share.getCreatedAt())
                .build();
    }
}
