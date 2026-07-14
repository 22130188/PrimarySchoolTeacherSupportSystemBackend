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
import vn.edu.primary.teacher_support.repository.PostCommentRepository;
import vn.edu.primary.teacher_support.entity.PostComment;
import vn.edu.primary.teacher_support.dto.PostCommentResponse;
import vn.edu.primary.teacher_support.dto.CreateCommentRequest;

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
    private final PostCommentRepository postCommentRepository;
    private final NotificationClient notificationClient;
    private final ActionLogClient actionLogClient;

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
        Classroom classroom = classroomService.requireWritableClassroom(classroomId);
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

        notifyAboutNewPost(classroom, saved, authorId);
        logPostAction("CREATE", saved, authorId, classroomId);

        Map<Long, UserDto> authors = userServiceClient.findById(authorId)
                .map(user -> Map.of(user.getId(), user))
                .orElse(Collections.emptyMap());

        return toResponse(saved, authors, authorId, classroom.getTeacherId());
    }

    @Transactional
    public ClassroomPostResponse updatePost(Long classroomId, Long postId, Long requesterId, CreateClassroomPostRequest request) {
        Classroom classroom = classroomService.requireWritableClassroom(classroomId);
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
        logPostAction("UPDATE", saved, requesterId, classroomId);

        Map<Long, UserDto> authors = userServiceClient.findById(requesterId)
                .map(user -> Map.of(user.getId(), user))
                .orElse(Collections.emptyMap());

        return toResponse(saved, authors, requesterId, classroom.getTeacherId());
    }

    @Transactional
    public void deletePost(Long classroomId, Long postId, Long requesterId) {
        Classroom classroom = classroomService.requireWritableClassroom(classroomId);
        validateCanView(classroom, requesterId);

        ClassroomPost post = classroomPostRepository.findByIdAndClassroomId(postId, classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài đăng"));

        boolean isClassTeacher = classroom.getTeacherId().equals(requesterId);
        boolean isAuthor = post.getAuthorId().equals(requesterId);
        if (!isClassTeacher && !isAuthor) {
            throw new ForbiddenException("Bạn không có quyền xóa bài đăng này");
        }

        logPostAction("DELETE", post, requesterId, classroomId);
        classroomPostRepository.delete(post);
    }

    private void logPostAction(String verb, ClassroomPost post, Long actorId, Long classroomId) {
        Classroom classroom = post.getClassroom();
        String classroomName = classroom != null && classroom.getName() != null ? classroom.getName() : ("#" + classroomId);
        PostType type = post.getPostType() == null ? PostType.ANNOUNCEMENT : post.getPostType();
        String action;
        String module;
        switch (type) {
            case ASSIGNMENT -> {
                action = verb + "_CLASSROOM_ASSIGNMENT";
                module = "assignments";
            }
            case TEST -> {
                action = verb + "_CLASSROOM_TEST";
                module = "tests";
            }
            default -> {
                action = verb + "_CLASSROOM_ANNOUNCEMENT";
                module = "posts";
            }
        }
        String username = userServiceClient.findById(actorId).map(UserDto::getUsername).orElse(null);
        String endpoint = "/api/classrooms/" + classroomId + "/posts"
                + (post.getId() != null && !"CREATE".equals(verb) ? "/" + post.getId() : "");
        String httpMethod = "CREATE".equals(verb) ? "POST" : "UPDATE".equals(verb) ? "PATCH" : "DELETE";
        String safeName = classroomName.replace("\\", "\\\\").replace("\"", "\\\"");
        actionLogClient.log(
                username,
                action,
                module,
                post.getId() == null ? null : String.valueOf(post.getId()),
                httpMethod,
                endpoint,
                "WARNING",
                "SUCCESS",
                "{\"postType\":\"" + type.name() + "\",\"classroomId\":" + classroomId
                        + ",\"classroomName\":\"" + safeName + "\"}"
        );
    }

    @Transactional(readOnly = true)
    public List<PostCommentResponse> getPostComments(Long classroomId, Long postId, Long requesterId) {
        Classroom classroom = classroomService.getActiveClassroom(classroomId);
        validateCanView(classroom, requesterId);

        ClassroomPost post = classroomPostRepository.findByIdAndClassroomId(postId, classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài đăng"));

        List<PostComment> comments = postCommentRepository.findByPostIdOrderByCreatedAtAsc(postId);

        Set<Long> authorIds = comments.stream().map(PostComment::getAuthorId).collect(Collectors.toSet());
        Map<Long, UserDto> authors = authorIds.stream()
                .map(userServiceClient::findById)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .collect(Collectors.toMap(UserDto::getId, u -> u));

        return comments.stream().map(comment -> {
            UserDto author = authors.get(comment.getAuthorId());
            boolean canDelete = requesterId != null &&
                    (requesterId.equals(comment.getAuthorId()) || requesterId.equals(classroom.getTeacherId()));

            return PostCommentResponse.builder()
                    .id(comment.getId())
                    .postId(comment.getPost().getId())
                    .authorId(comment.getAuthorId())
                    .authorName(author != null ? author.getUsername() : "Unknown")
                    .authorAvatarUrl(author != null ? author.getAvatarUrl() : null)
                    .content(comment.getContent())
                    .createdAt(comment.getCreatedAt())
                    .canDelete(canDelete)
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional
    public PostCommentResponse createPostComment(Long classroomId, Long postId, Long authorId, CreateCommentRequest request) {
        Classroom classroom = classroomService.requireWritableClassroom(classroomId);
        validateCanView(classroom, authorId);

        ClassroomPost post = classroomPostRepository.findByIdAndClassroomId(postId, classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài đăng"));

        PostComment comment = PostComment.builder()
                .post(post)
                .authorId(authorId)
                .content(request.getContent().trim())
                .build();

        PostComment saved = postCommentRepository.save(comment);

        UserDto author = userServiceClient.findById(authorId).orElse(null);

        if (!post.getAuthorId().equals(authorId)) {
            String authorName = author != null ? author.getUsername() : "Một thành viên";
            notificationClient.notifyUser(post.getAuthorId(), authorId, authorName,
                    "POST_COMMENT", authorName + " đã bình luận bài đăng của bạn",
                    classroom.getName(),
                    "/classrooms/" + classroomId + "?postId=" + postId,
                    "CLASSROOM_POST", postId);
        }

        return PostCommentResponse.builder()
                .id(saved.getId())
                .postId(saved.getPost().getId())
                .authorId(saved.getAuthorId())
                .authorName(author != null ? author.getUsername() : "Unknown")
                .authorAvatarUrl(author != null ? author.getAvatarUrl() : null)
                .content(saved.getContent())
                .createdAt(saved.getCreatedAt())
                .canDelete(true)
                .build();
    }

    @Transactional
    public void deletePostComment(Long classroomId, Long postId, Long commentId, Long requesterId) {
        Classroom classroom = classroomService.requireWritableClassroom(classroomId);
        validateCanView(classroom, requesterId);

        ClassroomPost post = classroomPostRepository.findByIdAndClassroomId(postId, classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài đăng"));

        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhận xét"));

        if (!comment.getPost().getId().equals(post.getId())) {
            throw new BusinessException("Nhận xét không thuộc bài đăng này");
        }

        boolean isClassTeacher = classroom.getTeacherId().equals(requesterId);
        boolean isAuthor = comment.getAuthorId().equals(requesterId);
        if (!isClassTeacher && !isAuthor) {
            throw new ForbiddenException("Bạn không có quyền xóa nhận xét này");
        }

        postCommentRepository.delete(comment);
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

    private void notifyAboutNewPost(Classroom classroom, ClassroomPost post, Long authorId) {
        UserDto author = userServiceClient.findById(authorId).orElse(null);
        String authorName = author != null ? author.getUsername() : "Giáo viên";
        String title = switch (post.getPostType()) {
            case ASSIGNMENT -> "Bài tập mới trong " + classroom.getName();
            case TEST -> "Bài kiểm tra mới trong " + classroom.getName();
            case ANNOUNCEMENT -> "Thông báo mới trong " + classroom.getName();
        };
        String type = switch (post.getPostType()) {
            case ASSIGNMENT -> "NEW_ASSIGNMENT";
            case TEST -> "NEW_TEST";
            case ANNOUNCEMENT -> "CLASS_ANNOUNCEMENT";
        };

        if (classroom.getTeacherId().equals(authorId)) {
            List<Long> studentIds = classroomMemberRepository
                    .findByClassroomIdAndStatusOrderByJoinedAtDesc(classroom.getId(), MemberStatus.ACTIVE)
                    .stream().map(member -> member.getStudentId()).toList();
            notificationClient.notifyUsers(studentIds, authorId, authorName, type, title,
                    post.getTitle() != null ? post.getTitle() : post.getContent(),
                    "/classrooms/" + classroom.getId() + "?postId=" + post.getId(),
                    "CLASSROOM_POST", post.getId());
        } else {
            notificationClient.notifyUser(classroom.getTeacherId(), authorId, authorName,
                    "STUDENT_POST", authorName + " đã đăng bài trong " + classroom.getName(),
                    post.getContent(),
                    "/classrooms/" + classroom.getId() + "?postId=" + post.getId(),
                    "CLASSROOM_POST", post.getId());
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

        long commentCount = postCommentRepository.countByPostId(post.getId());

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
                .commentCount(commentCount)
                .build();
    }
}
