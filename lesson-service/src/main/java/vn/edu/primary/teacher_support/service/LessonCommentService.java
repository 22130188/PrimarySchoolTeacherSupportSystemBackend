package vn.edu.primary.teacher_support.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.primary.teacher_support.dto.CreateLessonCommentRequest;
import vn.edu.primary.teacher_support.dto.LessonCommentResponse;
import vn.edu.primary.teacher_support.dto.UserDto;
import vn.edu.primary.teacher_support.entity.LessonClassroomShare;
import vn.edu.primary.teacher_support.entity.LessonComment;
import vn.edu.primary.teacher_support.exception.BusinessException;
import vn.edu.primary.teacher_support.exception.ForbiddenException;
import vn.edu.primary.teacher_support.exception.ResourceNotFoundException;
import vn.edu.primary.teacher_support.repository.LessonClassroomShareRepository;
import vn.edu.primary.teacher_support.repository.LessonCommentRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonCommentService {

    private final LessonCommentRepository commentRepository;
    private final LessonClassroomShareRepository classroomShareRepository;
    private final ClassroomServiceClient classroomServiceClient;
    private final UserServiceClient userServiceClient;
    private final NotificationClient notificationClient;

    @Transactional(readOnly = true)
    public List<LessonCommentResponse> getComments(Long classroomId, Long draftId, Long requesterId) {
        LessonClassroomShare share = getAccessibleShare(classroomId, draftId, requesterId);
        List<LessonComment> comments = commentRepository
                .findByClassroomShareIdOrderByCreatedAtAsc(share.getId());

        Set<Long> authorIds = comments.stream()
                .map(LessonComment::getAuthorId)
                .collect(Collectors.toSet());
        Map<Long, UserDto> authors = authorIds.stream()
                .map(userServiceClient::findById)
                .flatMap(java.util.Optional::stream)
                .collect(Collectors.toMap(UserDto::getId, Function.identity()));

        Long teacherId = classroomServiceClient.getClassroomTeacherId(classroomId);
        return comments.stream()
                .map(comment -> toResponse(comment, authors.get(comment.getAuthorId()), requesterId, teacherId))
                .toList();
    }

    @Transactional
    public LessonCommentResponse createComment(
            Long classroomId,
            Long draftId,
            Long authorId,
            CreateLessonCommentRequest request
    ) {
        LessonClassroomShare share = getAccessibleShare(classroomId, draftId, authorId);
        LessonComment saved = commentRepository.save(LessonComment.builder()
                .classroomShare(share)
                .authorId(authorId)
                .content(request.getContent().trim())
                .build());

        UserDto author = userServiceClient.findById(authorId).orElse(null);
        if (!share.getOwnerUserId().equals(authorId)) {
            String authorName = displayName(author);
            notificationClient.notifyUser(share.getOwnerUserId(), authorId, authorName,
                    "LESSON_COMMENT", authorName + " đã nhận xét bài giảng của bạn",
                    request.getContent().trim(),
                    "/classrooms/" + classroomId,
                    "LESSON", draftId);
        }
        return toResponse(saved, author, authorId, classroomServiceClient.getClassroomTeacherId(classroomId));
    }

    @Transactional
    public void deleteComment(Long classroomId, Long draftId, Long commentId, Long requesterId) {
        LessonClassroomShare share = getAccessibleShare(classroomId, draftId, requesterId);
        LessonComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhận xét"));

        if (!comment.getClassroomShare().getId().equals(share.getId())) {
            throw new BusinessException("Nhận xét không thuộc bài giảng này");
        }

        Long teacherId = classroomServiceClient.getClassroomTeacherId(classroomId);
        boolean isAuthor = requesterId.equals(comment.getAuthorId());
        boolean isClassTeacher = requesterId.equals(teacherId);
        if (!isAuthor && !isClassTeacher) {
            throw new ForbiddenException("Bạn không có quyền xóa nhận xét này");
        }

        commentRepository.delete(comment);
    }

    private LessonClassroomShare getAccessibleShare(Long classroomId, Long draftId, Long userId) {
        if (!classroomServiceClient.hasAccess(classroomId, userId)) {
            throw new ForbiddenException("Bạn không có quyền truy cập lớp học này");
        }
        return classroomShareRepository.findByDraftIdAndClassroomId(draftId, classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Bài giảng không được chia sẻ trong lớp này"));
    }

    private LessonCommentResponse toResponse(
            LessonComment comment,
            UserDto author,
            Long requesterId,
            Long teacherId
    ) {
        return LessonCommentResponse.builder()
                .id(comment.getId())
                .draftId(comment.getClassroomShare().getDraftId())
                .classroomId(comment.getClassroomShare().getClassroomId())
                .authorId(comment.getAuthorId())
                .authorName(displayName(author))
                .authorAvatarUrl(author != null ? author.getAvatarUrl() : null)
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .canDelete(requesterId != null &&
                        (requesterId.equals(comment.getAuthorId()) || requesterId.equals(teacherId)))
                .build();
    }

    private String displayName(UserDto user) {
        if (user == null) return "Unknown";
        if (user.getUsername() != null && !user.getUsername().isBlank()) return user.getUsername();
        return user.getEmail() != null ? user.getEmail() : "Unknown";
    }
}
