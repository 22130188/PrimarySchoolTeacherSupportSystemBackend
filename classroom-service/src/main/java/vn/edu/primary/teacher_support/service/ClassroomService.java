package vn.edu.primary.teacher_support.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.primary.teacher_support.dto.*;
import vn.edu.primary.teacher_support.entity.Classroom;
import vn.edu.primary.teacher_support.entity.ClassroomInvitation;
import vn.edu.primary.teacher_support.entity.ClassroomMember;
import vn.edu.primary.teacher_support.entity.enums.InvitationStatus;
import vn.edu.primary.teacher_support.entity.enums.MemberStatus;
import vn.edu.primary.teacher_support.exception.BusinessException;
import vn.edu.primary.teacher_support.exception.ForbiddenException;
import vn.edu.primary.teacher_support.exception.ResourceNotFoundException;
import vn.edu.primary.teacher_support.repository.ClassroomInvitationRepository;
import vn.edu.primary.teacher_support.repository.ClassroomMemberRepository;
import vn.edu.primary.teacher_support.repository.ClassroomRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final ClassroomMemberRepository memberRepository;
    private final ClassroomInvitationRepository invitationRepository;
    private final UserServiceClient userServiceClient;

    @Value("${classroom.frontend.base-url}")
    private String frontendBaseUrl;

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();

    @Transactional
    public ClassroomResponse createClassroom(CreateClassroomRequest request, Long teacherId) {
        Classroom classroom = Classroom.builder()
                .name(request.getName())
                .description(request.getDescription())
                .teacherId(teacherId)
                .createdBy(teacherId)
                .classCode(generateUniqueClassCode())
                .inviteLinkToken(generateUniqueInviteLinkToken())
                .build();

        classroom = classroomRepository.save(classroom);
        return toResponse(classroom);
    }

    public ClassroomResponse getClassroom(Long classroomId, Long userId) {
        Classroom classroom = getActiveClassroom(classroomId);
        return toResponse(classroom);
    }

    public List<ClassroomResponse> getMyClassrooms(Long teacherId) {
        List<Classroom> classrooms = classroomRepository
                .findByTeacherIdAndIsDeletedFalseOrderByCreatedAtDesc(teacherId);
        return classrooms.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public ClassroomResponse updateClassroom(Long classroomId, UpdateClassroomRequest request, Long teacherId) {
        Classroom classroom = getActiveClassroom(classroomId);
        validateTeacherOwnership(classroom, teacherId);

        classroom.setName(request.getName().trim());
        classroom.setDescription(request.getDescription());
        classroom = classroomRepository.save(classroom);

        return toResponse(classroom);
    }

    @Transactional
    public void deleteClassroom(Long classroomId, Long teacherId) {
        Classroom classroom = getActiveClassroom(classroomId);
        validateTeacherOwnership(classroom, teacherId);

        classroom.setIsDeleted(true);
        classroom.setDeletedAt(LocalDateTime.now());
        classroomRepository.save(classroom);
    }

    @Transactional
    public ClassroomResponse resetClassCode(Long classroomId, Long teacherId) {
        Classroom classroom = getActiveClassroom(classroomId);
        validateTeacherOwnership(classroom, teacherId);

        classroom.setClassCode(generateUniqueClassCode());
        classroom = classroomRepository.save(classroom);
        return toResponse(classroom);
    }

    @Transactional
    public ClassroomResponse resetInviteLink(Long classroomId, Long teacherId) {
        Classroom classroom = getActiveClassroom(classroomId);
        validateTeacherOwnership(classroom, teacherId);

        classroom.setInviteLinkToken(generateUniqueInviteLinkToken());
        classroom = classroomRepository.save(classroom);
        return toResponse(classroom);
    }

    public ClassroomRosterResponse getRoster(Long classroomId, Long teacherId) {
        Classroom classroom = getActiveClassroom(classroomId);
        validateTeacherOwnership(classroom, teacherId);

        // 1. Get joined students
        List<ClassroomMember> members = memberRepository
                .findByClassroomIdAndStatusOrderByJoinedAtDesc(classroomId, MemberStatus.ACTIVE);

        Set<Long> studentIds = members.stream()
                .map(ClassroomMember::getStudentId)
                .collect(Collectors.toSet());
        studentIds.add(classroom.getTeacherId());

        Map<Long, UserDto> userMap = new HashMap<>();
        for (Long sid : studentIds) {
            userServiceClient.findById(sid).ifPresent(u -> userMap.put(u.getId(), u));
        }

        UserDto teacherDto = userMap.get(classroom.getTeacherId());
        ClassroomRosterResponse.TeacherInfo teacherInfo = ClassroomRosterResponse.TeacherInfo.builder()
                .teacherId(classroom.getTeacherId())
                .name(teacherDto != null ? teacherDto.getUsername() : "Unknown")
                .email(teacherDto != null ? teacherDto.getEmail() : "")
                .avatarUrl(teacherDto != null ? teacherDto.getAvatarUrl() : null)
                .build();

        List<ClassroomRosterResponse.StudentMember> studentList = members.stream()
                .map(m -> {
                    UserDto u = userMap.get(m.getStudentId());
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

        // 2. Get invitations
        List<ClassroomInvitation> invitations = invitationRepository
                .findByClassroomIdAndStatuses(classroomId,
                        List.of(InvitationStatus.INVITED, InvitationStatus.WAITING_REGISTER));

        List<ClassroomRosterResponse.InvitedStudent> invitedList = new ArrayList<>();
        List<ClassroomRosterResponse.WaitingRegister> waitingList = new ArrayList<>();

        for (ClassroomInvitation inv : invitations) {
            if (inv.getStatus() == InvitationStatus.INVITED) {
                UserDto u = inv.getStudentId() != null ? userMap.get(inv.getStudentId()) : null;
                if (u == null && inv.getStudentId() != null) {
                    u = userServiceClient.findById(inv.getStudentId()).orElse(null);
                }
                invitedList.add(ClassroomRosterResponse.InvitedStudent.builder()
                        .invitationId(inv.getId())
                        .email(inv.getEmail())
                        .studentId(inv.getStudentId())
                        .studentName(u != null ? u.getUsername() : null)
                        .invitedAt(inv.getCreatedAt())
                        .status(inv.getStatus().name())
                        .expiredAt(inv.getExpiredAt())
                        .build());
            } else if (inv.getStatus() == InvitationStatus.WAITING_REGISTER) {
                waitingList.add(ClassroomRosterResponse.WaitingRegister.builder()
                        .invitationId(inv.getId())
                        .email(inv.getEmail())
                        .invitedAt(inv.getCreatedAt())
                        .status(inv.getStatus().name())
                        .build());
            }
        }

        return ClassroomRosterResponse.builder()
                .classroomId(classroom.getId())
                .classroomName(classroom.getName())
                .teacher(teacherInfo)
                .students(studentList)
                .invited(invitedList)
                .waitingRegister(waitingList)
                .build();
    }


    public ClassroomRosterResponse getRosterForMember(Long classroomId, Long studentId) {
        Classroom classroom = getActiveClassroom(classroomId);

        if (!memberRepository.existsByClassroomIdAndStudentIdAndStatus(
                classroomId, studentId, MemberStatus.ACTIVE)) {
            throw new ForbiddenException("Bạn không phải thành viên của lớp này");
        }

        // 1. Get joined students
        List<ClassroomMember> members = memberRepository
                .findByClassroomIdAndStatusOrderByJoinedAtDesc(classroomId, MemberStatus.ACTIVE);

        Set<Long> userIds = members.stream()
                .map(ClassroomMember::getStudentId)
                .collect(Collectors.toSet());
        userIds.add(classroom.getTeacherId());

        Map<Long, UserDto> userMap = new HashMap<>();
        for (Long uid : userIds) {
            userServiceClient.findById(uid).ifPresent(u -> userMap.put(u.getId(), u));
        }

        // Teacher info
        UserDto teacherDto = userMap.get(classroom.getTeacherId());
        ClassroomRosterResponse.TeacherInfo teacherInfo = ClassroomRosterResponse.TeacherInfo.builder()
                .teacherId(classroom.getTeacherId())
                .name(teacherDto != null ? teacherDto.getUsername() : "Unknown")
                .email(teacherDto != null ? teacherDto.getEmail() : "")
                .avatarUrl(teacherDto != null ? teacherDto.getAvatarUrl() : null)
                .build();

        // Student list
        List<ClassroomRosterResponse.StudentMember> studentList = members.stream()
                .map(m -> {
                    UserDto u = userMap.get(m.getStudentId());
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
                .invited(List.of()) // Students don't see invitations
                .waitingRegister(List.of()) // Students don't see waiting list
                .build();
    }

    @Transactional
    public void removeStudent(Long classroomId, Long memberId, Long teacherId) {
        Classroom classroom = getActiveClassroom(classroomId);
        validateTeacherOwnership(classroom, teacherId);

        ClassroomMember member = memberRepository.findByIdAndClassroomId(memberId, classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thành viên"));

        member.setStatus(MemberStatus.REMOVED);
        memberRepository.save(member);
    }


    public Classroom findByInviteLinkToken(String token) {
        return classroomRepository.findByInviteLinkTokenAndIsDeletedFalse(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invite link không hợp lệ hoặc lớp đã bị xóa"));
    }

    public Classroom findByClassCode(String code) {
        return classroomRepository.findByClassCodeAndIsDeletedFalse(code)
                .orElseThrow(() -> new ResourceNotFoundException("Class code không hợp lệ hoặc lớp đã bị xóa"));
    }

    public Classroom getActiveClassroom(Long id) {
        return classroomRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học với ID: " + id));
    }


    private void validateTeacherOwnership(Classroom classroom, Long teacherId) {
        if (!classroom.getTeacherId().equals(teacherId)) {
            throw new ForbiddenException("Bạn không phải giáo viên của lớp này");
        }
    }

    private ClassroomResponse toResponse(Classroom classroom) {
        int studentCount = memberRepository
                .findByClassroomIdAndStatusOrderByJoinedAtDesc(classroom.getId(), MemberStatus.ACTIVE).size();

        UserDto teacher = userServiceClient.findById(classroom.getTeacherId()).orElse(null);

        String inviteLink = frontendBaseUrl + "/join/link?token=" + classroom.getInviteLinkToken();

        return ClassroomResponse.builder()
                .id(classroom.getId())
                .name(classroom.getName())
                .description(classroom.getDescription())
                .teacherId(classroom.getTeacherId())
                .teacherName(teacher != null ? teacher.getUsername() : "Unknown")
                .teacherEmail(teacher != null ? teacher.getEmail() : "")
                .classCode(classroom.getClassCode())
                .inviteLink(inviteLink)
                .studentCount(studentCount)
                .createdAt(classroom.getCreatedAt())
                .updatedAt(classroom.getUpdatedAt())
                .build();
    }

    private String generateUniqueClassCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
            }
            code = sb.toString();
        } while (classroomRepository.existsByClassCode(code));
        return code;
    }

    private String generateUniqueInviteLinkToken() {
        String token;
        do {
            token = UUID.randomUUID().toString().replace("-", "");
        } while (classroomRepository.existsByInviteLinkToken(token));
        return token;
    }
}
