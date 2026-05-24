package vn.edu.primary.teacher_support.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.primary.teacher_support.dto.ClassroomPostResponse;
import vn.edu.primary.teacher_support.dto.CreateClassroomPostRequest;
import vn.edu.primary.teacher_support.dto.UserDto;
import vn.edu.primary.teacher_support.entity.Classroom;
import vn.edu.primary.teacher_support.entity.ClassroomPost;
import vn.edu.primary.teacher_support.entity.ClassroomPostAttachment;
import vn.edu.primary.teacher_support.entity.enums.MemberStatus;
import vn.edu.primary.teacher_support.entity.enums.PostType;
import vn.edu.primary.teacher_support.exception.BusinessException;
import vn.edu.primary.teacher_support.exception.ForbiddenException;
import vn.edu.primary.teacher_support.exception.ResourceNotFoundException;
import vn.edu.primary.teacher_support.repository.ClassroomMemberRepository;
import vn.edu.primary.teacher_support.repository.ClassroomPostRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassroomPostService {

    private static final int MAX_ATTACHMENTS = 10;

    private final ClassroomService classroomService;
    private final ClassroomPostRepository classroomPostRepository;
    private final ClassroomMemberRepository classroomMemberRepository;
    private final UserServiceClient userServiceClient;
    private final GoogleDriveService googleDriveService;

    @Transactional(readOnly = true)
    public List<ClassroomPostResponse> getPosts(Long classroomId, Long requesterId, int limit) {
        Classroom classroom = classroomService.getActiveClassroom(classroomId);
        validateCanView(classroom, requesterId);

        int normalizedLimit = Math.min(Math.max(limit, 1), 100);

        List<ClassroomPost> posts = classroomPostRepository.findByClassroomIdOrderByCreatedAtDesc(
                classroomId,
                PageRequest.of(0, normalizedLimit)
        );

        Set<Long> authorIds = posts.stream().map(ClassroomPost::getAuthorId).collect(Collectors.toSet());
        Map<Long, UserDto> authors = authorIds.stream()
                .map(userServiceClient::findById)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .collect(Collectors.toMap(UserDto::getId, u -> u));

        return posts.stream()
                .map(post -> toResponse(post, authors, requesterId, classroom.getTeacherId()))
                .collect(Collectors.toList());
    }

    @Transactional
    public ClassroomPostResponse createPost(Long classroomId, Long authorId, CreateClassroomPostRequest request) {
        Classroom classroom = classroomService.getActiveClassroom(classroomId);
        validateCanView(classroom, authorId);

        String content = request.getContent() == null ? "" : request.getContent().trim();
        List<vn.edu.primary.teacher_support.dto.DriveAttachmentRequest> attachmentRequests =
                request.getAttachments() == null ? Collections.emptyList() : request.getAttachments();

        if (content.isBlank() && attachmentRequests.isEmpty()) {
            throw new BusinessException("Bài đăng phải có nội dung hoặc tệp đính kèm");
        }

        if (attachmentRequests.size() > MAX_ATTACHMENTS) {
            throw new BusinessException("Tối đa " + MAX_ATTACHMENTS + " tệp đính kèm cho mỗi bài đăng");
        }

        ClassroomPost post = ClassroomPost.builder()
                .classroom(classroom)
                .authorId(authorId)
                .postType(request.getPostType() == null ? PostType.ANNOUNCEMENT : request.getPostType())
                .title(request.getTitle())
                .attemptLimit(request.getPostType() == PostType.TEST ? 1 : request.getAttemptLimit())
                .questionCount(request.getQuestionCount())
                .maxPoints(request.getMaxPoints())
                .startAt(request.getStartAt())
                .durationMinutes(request.getDurationMinutes())
                .referenceTestId(request.getReferenceTestId())
                .referenceTestName(request.getReferenceTestName())
                .content(content.isBlank() ? null : content)
                .build();

        for (vn.edu.primary.teacher_support.dto.DriveAttachmentRequest attachmentRequest : attachmentRequests) {
            GoogleDriveService.ResolvedDriveAttachment resolved = googleDriveService.resolveAttachment(attachmentRequest);
            ClassroomPostAttachment attachment = ClassroomPostAttachment.builder()
                    .post(post)
                    .driveFileId(resolved.getDriveFileId())
                    .name(resolved.getName())
                    .mimeType(resolved.getMimeType())
                    .sizeBytes(resolved.getSizeBytes())
                    .iconLink(resolved.getIconLink())
                    .thumbnailLink(resolved.getThumbnailLink())
                    .webViewLink(resolved.getWebViewLink())
                    .webContentLink(resolved.getWebContentLink())
                    .build();
            post.getAttachments().add(attachment);
        }

        ClassroomPost saved = classroomPostRepository.save(post);

        Map<Long, UserDto> authors = userServiceClient.findById(authorId)
                .map(user -> Map.of(user.getId(), user))
                .orElse(Collections.emptyMap());

        return toResponse(saved, authors, authorId, classroom.getTeacherId());
    }

    @Transactional
    public ClassroomPostResponse updatePost(Long classroomId, Long postId, Long requesterId, CreateClassroomPostRequest request) {
        Classroom classroom = classroomService.getActiveClassroom(classroomId);
        validateCanView(classroom, requesterId);

        ClassroomPost post = classroomPostRepository.findByIdAndClassroomId(postId, classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài đăng"));

        boolean isClassTeacher = classroom.getTeacherId().equals(requesterId);
        boolean isAuthor = post.getAuthorId().equals(requesterId);
        if (!isClassTeacher && !isAuthor) {
            throw new ForbiddenException("Bạn không có quyền cập nhật bài đăng này");
        }

        String content = request.getContent() == null ? "" : request.getContent().trim();
        List<vn.edu.primary.teacher_support.dto.DriveAttachmentRequest> attachmentRequests =
                request.getAttachments() == null ? Collections.emptyList() : request.getAttachments();

        if (content.isBlank() && attachmentRequests.isEmpty() && request.getTitle() == null) {
            throw new BusinessException("Bài đăng phải có nội dung, tiêu đề hoặc tệp đính kèm");
        }

        post.setPostType(request.getPostType() == null ? PostType.ANNOUNCEMENT : request.getPostType());
        post.setTitle(request.getTitle());
        post.setAttemptLimit(request.getPostType() == PostType.TEST ? 1 : request.getAttemptLimit());
        post.setQuestionCount(request.getQuestionCount());
        post.setMaxPoints(request.getMaxPoints());
        post.setStartAt(request.getStartAt());
        post.setDurationMinutes(request.getDurationMinutes());
        post.setReferenceTestId(request.getReferenceTestId());
        post.setReferenceTestName(request.getReferenceTestName());
        post.setContent(content.isBlank() ? null : content);

        if (!attachmentRequests.isEmpty()) {
            post.getAttachments().clear();
            for (vn.edu.primary.teacher_support.dto.DriveAttachmentRequest attachmentRequest : attachmentRequests) {
                GoogleDriveService.ResolvedDriveAttachment resolved = googleDriveService.resolveAttachment(attachmentRequest);
                ClassroomPostAttachment attachment = ClassroomPostAttachment.builder()
                        .post(post)
                        .driveFileId(resolved.getDriveFileId())
                        .name(resolved.getName())
                        .mimeType(resolved.getMimeType())
                        .sizeBytes(resolved.getSizeBytes())
                        .iconLink(resolved.getIconLink())
                        .thumbnailLink(resolved.getThumbnailLink())
                        .webViewLink(resolved.getWebViewLink())
                        .webContentLink(resolved.getWebContentLink())
                        .build();
                post.getAttachments().add(attachment);
            }
        }

        ClassroomPost saved = classroomPostRepository.save(post);

        Map<Long, UserDto> authors = userServiceClient.findById(requesterId)
                .map(user -> Map.of(user.getId(), user))
                .orElse(Collections.emptyMap());

        return toResponse(saved, authors, requesterId, classroom.getTeacherId());
    }

    @Transactional
    public void deletePost(Long classroomId, Long postId, Long requesterId) {
        Classroom classroom = classroomService.getActiveClassroom(classroomId);
        validateCanView(classroom, requesterId);

        ClassroomPost post = classroomPostRepository.findByIdAndClassroomId(postId, classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài đăng"));

        boolean isClassTeacher = classroom.getTeacherId().equals(requesterId);
        boolean isAuthor = post.getAuthorId().equals(requesterId);
        if (!isClassTeacher && !isAuthor) {
            throw new ForbiddenException("Bạn không có quyền xóa bài đăng này");
        }

        classroomPostRepository.delete(post);
    }

    private void validateCanView(Classroom classroom, Long userId) {
        if (classroom.getTeacherId().equals(userId)) {
            return;
        }

        boolean activeMember = classroomMemberRepository.existsByClassroomIdAndStudentIdAndStatus(
                classroom.getId(),
                userId,
                MemberStatus.ACTIVE
        );
        if (!activeMember) {
            throw new ForbiddenException("Bạn không phải thành viên của lớp này");
        }
    }

    private ClassroomPostResponse toResponse(
            ClassroomPost post,
            Map<Long, UserDto> authorMap,
            Long requesterId,
            Long teacherId
    ) {
        UserDto author = authorMap.get(post.getAuthorId());
        boolean canDelete = requesterId != null
                && (requesterId.equals(post.getAuthorId()) || requesterId.equals(teacherId));

        List<ClassroomPostResponse.AttachmentItem> attachments = post.getAttachments().stream()
                .map(attachment -> ClassroomPostResponse.AttachmentItem.builder()
                        .id(attachment.getId())
                        .driveFileId(attachment.getDriveFileId())
                        .name(attachment.getName())
                        .mimeType(attachment.getMimeType())
                        .sizeBytes(attachment.getSizeBytes())
                        .iconLink(attachment.getIconLink())
                        .thumbnailLink(attachment.getThumbnailLink())
                        .webViewLink(attachment.getWebViewLink())
                        .webContentLink(attachment.getWebContentLink())
                        .build())
                .collect(Collectors.toList());

        return ClassroomPostResponse.builder()
                .id(post.getId())
                .classroomId(post.getClassroom().getId())
                .authorId(post.getAuthorId())
                .authorName(author != null ? author.getUsername() : "Unknown")
                .authorAvatarUrl(author != null ? author.getAvatarUrl() : null)
                .postType(post.getPostType())
                .title(post.getTitle())
                .attemptLimit(post.getAttemptLimit())
                .questionCount(post.getQuestionCount())
                .maxPoints(post.getMaxPoints())
                .startAt(post.getStartAt())
                .durationMinutes(post.getDurationMinutes())
                .referenceTestId(post.getReferenceTestId())
                .referenceTestName(post.getReferenceTestName())
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .canDelete(canDelete)
                .attachments(attachments)
                .build();
    }
}
