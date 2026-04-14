package vn.edu.primary.teacher_support.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.primary.teacher_support.dto.*;
import vn.edu.primary.teacher_support.entity.Classroom;
import vn.edu.primary.teacher_support.entity.ClassroomMember;
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

    @Value("${classroom.frontend.base-url}")
    private String frontendBaseUrl;

    private static final List<InvitationStatus> PENDING_STATUSES =
            List.of(InvitationStatus.INVITED, InvitationStatus.WAITING_REGISTER);

    public List<AdminClassroomResponse> getAllClassrooms(boolean includeDeleted) {
        List<Classroom> classrooms;
        if (includeDeleted) {
            classrooms = classroomRepository.findAllByOrderByCreatedAtDesc();
        } else {
            classrooms = classroomRepository.findByIsDeletedFalseOrderByCreatedAtDesc();
        }
        return classrooms.stream()
                .map(this::toAdminResponse)
                .collect(Collectors.toList());
    }

    public AdminClassroomResponse getClassroomDetail(Long id) {
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học với ID: " + id));
        return toAdminResponse(classroom);
    }

    @Transactional
    public AdminClassroomResponse updateClassroom(Long id, UpdateClassroomRequest request) {
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học với ID: " + id));

        if (Boolean.TRUE.equals(classroom.getIsDeleted())) {
            throw new BusinessException("Không thể chỉnh sửa lớp đã bị xóa. Vui lòng khôi phục trước.");
        }

        classroom.setName(request.getName().trim());
        classroom.setDescription(request.getDescription());
        classroom = classroomRepository.save(classroom);

        log.info("Admin updated classroom {}: name='{}', description='{}'",
                id, request.getName(), request.getDescription());
        return toAdminResponse(classroom);
    }

    @Transactional
    public void softDeleteClassroom(Long id) {
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học với ID: " + id));

        if (Boolean.TRUE.equals(classroom.getIsDeleted())) {
            throw new BusinessException("Lớp học đã được xóa trước đó");
        }

        classroom.setIsDeleted(true);
        classroom.setDeletedAt(LocalDateTime.now());
        classroomRepository.save(classroom);

        log.info("Admin soft-deleted classroom {}", id);
    }

    @Transactional
    public AdminClassroomResponse restoreClassroom(Long id) {
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học với ID: " + id));

        if (!Boolean.TRUE.equals(classroom.getIsDeleted())) {
            throw new BusinessException("Lớp học chưa bị xóa, không cần khôi phục");
        }

        classroom.setIsDeleted(false);
        classroom.setDeletedAt(null);
        classroom = classroomRepository.save(classroom);

        log.info("Admin restored classroom {}", id);
        return toAdminResponse(classroom);
    }

    @Transactional
    public void hardDeleteClassroom(Long id) {
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học với ID: " + id));

        classroomRepository.delete(classroom);
        log.warn("Admin permanently deleted classroom {} (name='{}')", id, classroom.getName());
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
        Classroom classroom = classroomRepository.findById(classroomId)
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

    @Transactional
    public void removeMember(Long classroomId, Long memberId) {
        classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học với ID: " + classroomId));

        ClassroomMember member = memberRepository.findByIdAndClassroomId(memberId, classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thành viên"));

        member.setStatus(MemberStatus.REMOVED);
        memberRepository.save(member);

        log.info("Admin removed member {} from classroom {}", memberId, classroomId);
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
                .createdBy(classroom.getCreatedBy())
                .isDeleted(classroom.getIsDeleted())
                .createdAt(classroom.getCreatedAt())
                .updatedAt(classroom.getUpdatedAt())
                .deletedAt(classroom.getDeletedAt())
                .build();
    }
}
