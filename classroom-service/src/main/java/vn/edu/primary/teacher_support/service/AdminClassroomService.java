package vn.edu.primary.teacher_support.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.primary.teacher_support.dto.*;
import vn.edu.primary.teacher_support.entity.Classroom;
import vn.edu.primary.teacher_support.entity.ClassroomMember;
import vn.edu.primary.teacher_support.entity.enums.ClassroomStatus;
import vn.edu.primary.teacher_support.entity.enums.InvitationStatus;
import vn.edu.primary.teacher_support.entity.enums.MemberStatus;
import vn.edu.primary.teacher_support.exception.BusinessException;
import vn.edu.primary.teacher_support.exception.ResourceNotFoundException;
import vn.edu.primary.teacher_support.repository.ClassroomInvitationRepository;
import vn.edu.primary.teacher_support.repository.ClassroomMemberRepository;
import vn.edu.primary.teacher_support.repository.ClassroomRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminClassroomService {

    private final ClassroomRepository classroomRepository;
    private final ClassroomMemberRepository memberRepository;
    private final ClassroomInvitationRepository invitationRepository;
    private final UserServiceClient userServiceClient;
    private final NotificationClient notificationClient;
    private final EmailService emailService;
    private final ActionLogClient actionLogClient;

    @Value("${classroom.frontend.base-url}")
    private String frontendBaseUrl;

    private static final List<InvitationStatus> PENDING_STATUSES =
            List.of(InvitationStatus.INVITED, InvitationStatus.WAITING_REGISTER);

    public List<AdminClassroomResponse> getAllClassrooms() {
        return classroomRepository.findByIsDeletedFalseOrderByCreatedAtDesc().stream()
                .map(this::toAdminResponse)
                .collect(Collectors.toList());
    }
    public AdminClassroomResponse getClassroomDetail(Long id) {
        Classroom classroom = classroomRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học với ID: " + id));
        return toAdminResponse(classroom);
    }

    @Transactional
    public AdminClassroomResponse updateClassroom(Long id, UpdateClassroomRequest request) {
        Classroom classroom = classroomRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học với ID: " + id));

        if (Boolean.TRUE.equals(classroom.getIsDeleted())) {
            throw new BusinessException("Không thể chỉnh sửa lớp đã bị xóa. Vui lòng khôi phục trước.");
        }

        if (effectiveStatus(classroom) == ClassroomStatus.ARCHIVED) {
            throw new BusinessException("Lớp học đã lưu trữ chỉ có thể xem thông tin");
        }

        classroom.setName(request.getName().trim());
        classroom.setDescription(request.getDescription());
        classroom.setGradeLevel(request.getGradeLevel());
        classroom.setSubject(request.getSubject());
        classroom = classroomRepository.save(classroom);

        log.info("Admin updated classroom {}: name='{}', description='{}'",
                id, request.getName(), request.getDescription());
        return toAdminResponse(classroom);
    }

    @Transactional
    public AdminClassroomResponse lockClassroom(Long id, String reason, Long adminId,
            String authorization, String ipAddress) {
        Classroom classroom = findClassroom(id);
        ClassroomStatus beforeStatus = effectiveStatus(classroom);
        if (beforeStatus == ClassroomStatus.LOCKED) {
            throw new BusinessException("Lớp học đã bị khóa");
        }

        classroom.setStatusBeforeLock(beforeStatus);
        classroom.setStatus(ClassroomStatus.LOCKED);
        classroom = classroomRepository.save(classroom);
        notifyStatusChange(classroom, adminId, "CLASS_LOCKED", "Lớp học đã bị khóa",
                "Quản trị viên đã khóa lớp học.", reason);
        logStatusAction(classroom, "LOCK_CLASSROOM", reason, beforeStatus,
                ClassroomStatus.LOCKED, authorization, ipAddress);
        return toAdminResponse(classroom);
    }

    @Transactional
    public AdminClassroomResponse unlockClassroom(Long id, String reason, Long adminId,
            String authorization, String ipAddress) {
        Classroom classroom = findClassroom(id);
        if (effectiveStatus(classroom) != ClassroomStatus.LOCKED) {
            throw new BusinessException("Lớp học chưa bị khóa");
        }

        ClassroomStatus restoredStatus = classroom.getStatusBeforeLock();
        if (restoredStatus == null || restoredStatus == ClassroomStatus.LOCKED) {
            restoredStatus = ClassroomStatus.ACTIVE;
        }
        classroom.setStatus(restoredStatus);
        classroom.setStatusBeforeLock(null);
        classroom = classroomRepository.save(classroom);
        notifyStatusChange(classroom, adminId, "CLASS_UNLOCKED", "Lớp học đã được mở khóa",
                "Quản trị viên đã mở khóa lớp học.", reason);
        logStatusAction(classroom, "UNLOCK_CLASSROOM", reason, ClassroomStatus.LOCKED,
                restoredStatus, authorization, ipAddress);
        return toAdminResponse(classroom);
    }

    public AdminDashboardStats getDashboardStats() {
        long totalClassrooms = classroomRepository.countByIsDeletedFalse();
        long totalDeleted = classroomRepository.countByIsDeletedTrue();
        long totalMembers = memberRepository.countByStatus(MemberStatus.ACTIVE);
        long totalPendingInvitations = invitationRepository.countByStatusIn(PENDING_STATUSES);

        List<AdminClassroomResponse> recentClassrooms = classroomRepository
                .findTop5ByIsDeletedFalseOrderByCreatedAtDesc()
                .stream()
                .map(this::toAdminResponse)
                .collect(Collectors.toList());

        return AdminDashboardStats.builder()
                .totalClassrooms(totalClassrooms)
                .totalDeletedClassrooms(totalDeleted)
                .totalMembers(totalMembers)
                .totalPendingInvitations(totalPendingInvitations)
                .recentClassrooms(recentClassrooms)
                .build();
    }

    public ClassroomRosterResponse getClassroomMembers(Long classroomId) {
        Classroom classroom = classroomRepository.findByIdAndIsDeletedFalse(classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học với ID: " + classroomId));

        List<ClassroomMember> members = memberRepository
                .findByClassroomIdAndStatusOrderByJoinedAtDesc(classroomId, MemberStatus.ACTIVE);

        UserDto teacherDto = userServiceClient.findById(classroom.getTeacherId()).orElse(null);
        ClassroomRosterResponse.TeacherInfo teacherInfo = ClassroomRosterResponse.TeacherInfo.builder()
                .teacherId(classroom.getTeacherId())
                .name(teacherDto != null ? teacherDto.getUsername() : "Unknown")
                .email(teacherDto != null ? teacherDto.getEmail() : "")
                .avatarUrl(teacherDto != null ? teacherDto.getAvatarUrl() : null)
                .build();

        List<ClassroomRosterResponse.StudentMember> studentList = members.stream()
                .map(m -> {
                    UserDto u = userServiceClient.findById(m.getStudentId()).orElse(null);
                    return ClassroomRosterResponse.StudentMember.builder()
                            .memberId(m.getId())
                            .studentId(m.getStudentId())
                            .name(u != null ? u.getUsername() : "Unknown")
                            .email(u != null ? u.getEmail() : "")
                            .avatarUrl(u != null ? u.getAvatarUrl() : null)
                            .joinedAt(m.getJoinedAt())
                            .joinType(m.getJoinType().name())
                            .build();
                })
                .collect(Collectors.toList());

        return ClassroomRosterResponse.builder()
                .classroomId(classroom.getId())
                .classroomName(classroom.getName())
                .teacher(teacherInfo)
                .students(studentList)
                .invited(List.of())
                .waitingRegister(List.of())
                .build();
    }

    private Classroom findClassroom(Long id) {
        return classroomRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học với ID: " + id));
    }

    private ClassroomStatus effectiveStatus(Classroom classroom) {
        return classroom.getStatus() == null ? ClassroomStatus.ACTIVE : classroom.getStatus();
    }

    private void notifyStatusChange(Classroom classroom, Long adminId, String type,
            String title, String message, String reason) {
        List<ClassroomMember> members = memberRepository
                .findByClassroomIdAndStatusOrderByJoinedAtDesc(classroom.getId(), MemberStatus.ACTIVE);
        java.util.ArrayList<Long> targetIds = new java.util.ArrayList<>();
        targetIds.add(classroom.getTeacherId());
        members.stream().map(ClassroomMember::getStudentId).forEach(targetIds::add);
        notificationClient.notifyUsers(targetIds, adminId, "Quản trị viên", type, title,
                message + " Lý do: " + reason, "/classrooms", "CLASSROOM", classroom.getId());

        userServiceClient.findById(classroom.getTeacherId())
                .ifPresent(user -> emailService.sendClassroomStatusEmail(
                        user.getEmail(), classroom.getName(), title, message, reason));
        members.stream().map(ClassroomMember::getStudentId).distinct()
                .map(userServiceClient::findById).flatMap(java.util.Optional::stream)
                .forEach(user -> emailService.sendClassroomStatusEmail(
                        user.getEmail(), classroom.getName(), title, message, reason));
    }

    private void logStatusAction(Classroom classroom, String action, String reason,
            ClassroomStatus beforeStatus, ClassroomStatus afterStatus,
            String authorization, String ipAddress) {
        String description = """
                {"classroomId":%d,"classroomName":"%s","classCode":"%s","reason":"%s","statusBefore":"%s","statusAfter":"%s"}
                """.formatted(classroom.getId(), json(classroom.getName()), json(classroom.getClassCode()),
                        json(reason), beforeStatus, afterStatus).trim();
        actionLogClient.logAuthenticated(authorization, action, String.valueOf(classroom.getId()),
                "POST", "/api/admin/classrooms/" + classroom.getId()
                        + (action.equals("LOCK_CLASSROOM") ? "/lock" : "/unlock"),
                "DANGER", description, ipAddress);
    }

    private String json(String value) {
        if (value == null) return "";
        String slash = Character.toString(92);
        String quote = Character.toString(34);
        return value.replace(slash, slash + slash).replace(quote, slash + quote);
    }
    private AdminClassroomResponse toAdminResponse(Classroom classroom) {
        int studentCount = (int) memberRepository
                .countByClassroomIdAndStatus(classroom.getId(), MemberStatus.ACTIVE);

        int pendingCount = (int) invitationRepository
                .countByClassroomIdAndStatusIn(classroom.getId(), PENDING_STATUSES);

        UserDto teacher = userServiceClient.findById(classroom.getTeacherId()).orElse(null);

        String inviteLink = frontendBaseUrl + "/join/link?token=" + classroom.getInviteLinkToken();

        return AdminClassroomResponse.builder()
                .id(classroom.getId())
                .name(classroom.getName())
                .description(classroom.getDescription())
                .teacherId(classroom.getTeacherId())
                .teacherName(teacher != null ? teacher.getUsername() : "Unknown")
                .teacherEmail(teacher != null ? teacher.getEmail() : "")
                .classCode(classroom.getClassCode())
                .inviteLink(inviteLink)
                .studentCount(studentCount)
                .pendingInvitationCount(pendingCount)
                .gradeLevel(classroom.getGradeLevel())
                .subject(classroom.getSubject())
                .createdBy(classroom.getCreatedBy())
                .isDeleted(classroom.getIsDeleted())
                .status(effectiveStatus(classroom).name())
                .statusBeforeLock(classroom.getStatusBeforeLock() == null ? null : classroom.getStatusBeforeLock().name())
                .createdAt(classroom.getCreatedAt())
                .updatedAt(classroom.getUpdatedAt())
                .deletedAt(classroom.getDeletedAt())
                .build();
    }
}
