package vn.edu.primary.teacher_support.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.primary.teacher_support.dto.ClassroomResponse;
import vn.edu.primary.teacher_support.dto.UserDto;
import vn.edu.primary.teacher_support.entity.Classroom;
import vn.edu.primary.teacher_support.entity.ClassroomInvitation;
import vn.edu.primary.teacher_support.entity.ClassroomMember;
import vn.edu.primary.teacher_support.entity.enums.InvitationStatus;
import vn.edu.primary.teacher_support.entity.enums.JoinType;
import vn.edu.primary.teacher_support.entity.enums.MemberStatus;
import vn.edu.primary.teacher_support.exception.BusinessException;
import vn.edu.primary.teacher_support.exception.ResourceNotFoundException;
import vn.edu.primary.teacher_support.repository.ClassroomInvitationRepository;
import vn.edu.primary.teacher_support.repository.ClassroomMemberRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MembershipService {

    private final ClassroomMemberRepository memberRepository;
    private final ClassroomInvitationRepository invitationRepository;
    private final ClassroomService classroomService;
    private final InvitationService invitationService;
    private final UserServiceClient userServiceClient;
    private final NotificationClient notificationClient;


    @Transactional
    public ClassroomResponse joinByInviteLink(String token, Long studentId) {
        Classroom classroom = classroomService.findByInviteLinkToken(token);
        validateNotAlreadyMember(classroom.getId(), studentId);

        addOrReactivateMember(classroom, studentId, JoinType.INVITE_LINK);
        notifyTeacherAboutMembership(classroom, studentId, "đã tham gia lớp", "STUDENT_JOINED");

        log.info("Student {} joined classroom {} via invite link", studentId, classroom.getId());
        return classroomService.getClassroom(classroom.getId(), studentId);
    }


    @Transactional
    public ClassroomResponse joinByClassCode(String classCode, Long studentId) {
        Classroom classroom = classroomService.findByClassCode(classCode.trim().toUpperCase());
        validateNotAlreadyMember(classroom.getId(), studentId);

        addOrReactivateMember(classroom, studentId, JoinType.CLASS_CODE);
        notifyTeacherAboutMembership(classroom, studentId, "đã tham gia lớp", "STUDENT_JOINED");

        log.info("Student {} joined classroom {} via class code", studentId, classroom.getId());
        return classroomService.getClassroom(classroom.getId(), studentId);
    }


    @Transactional
    public ClassroomResponse joinByInvitationToken(String token, Long studentId, String email) {
        ClassroomInvitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lời mời"));

        if (!invitation.getEmail().equalsIgnoreCase(email)) {
            throw new BusinessException("Lời mời không dành cho tài khoản này");
        }

        if (invitation.getStatus() != InvitationStatus.INVITED) {
            throw new BusinessException("Lời mời không hợp lệ (trạng thái: " + invitation.getStatus() + ")");
        }

        Classroom classroom = invitation.getClassroom();
        classroomService.ensureWritable(classroom);
        validateNotAlreadyMember(classroom.getId(), studentId);

        invitationService.acceptInvitation(invitation.getId(), studentId, email);

        addOrReactivateMember(classroom, studentId, JoinType.EMAIL_INVITE);
        notifyTeacherAboutMembership(classroom, studentId, "đã chấp nhận lời mời vào lớp", "INVITATION_ACCEPTED");

        log.info("Student {} joined classroom {} via email invitation", studentId, classroom.getId());
        return classroomService.getClassroom(classroom.getId(), studentId);
    }


    @Transactional
    public ClassroomResponse acceptInvitationFromSystem(Long invitationId, Long studentId, String email) {
        ClassroomInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lời mời"));

        if (!invitation.getEmail().equalsIgnoreCase(email)) {
            throw new BusinessException("Lời mời không dành cho tài khoản này");
        }

        Classroom classroom = invitation.getClassroom();
        classroomService.ensureWritable(classroom);
        validateNotAlreadyMember(classroom.getId(), studentId);

        invitationService.acceptInvitation(invitationId, studentId, email);

        addOrReactivateMember(classroom, studentId, JoinType.EMAIL_INVITE);
        notifyTeacherAboutMembership(classroom, studentId, "đã chấp nhận lời mời vào lớp", "INVITATION_ACCEPTED");

        log.info("Student {} accepted invitation and joined classroom {}", studentId, classroom.getId());
        return classroomService.getClassroom(classroom.getId(), studentId);
    }


    public List<ClassroomResponse> getMyJoinedClassrooms(Long studentId) {
        List<ClassroomMember> members = memberRepository
                .findByStudentIdAndStatusAndClassroomIsDeletedFalseOrderByJoinedAtDesc(studentId, MemberStatus.ACTIVE);

        return members.stream()
                .map(member -> {
                    try {
                        return classroomService.getClassroom(member.getClassroom().getId(), studentId);
                    } catch (ResourceNotFoundException ex) {
                        log.warn("Skipping stale classroom membership {} for student {}: {}",
                                member.getId(), studentId, ex.getMessage());
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }


    @Transactional
    public void leaveClassroom(Long classroomId, Long studentId) {
        classroomService.requireWritableClassroom(classroomId);
        ClassroomMember member = memberRepository
                .findByClassroomIdAndStudentIdAndStatus(classroomId, studentId, MemberStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Bạn không phải thành viên của lớp này"));

        member.setStatus(MemberStatus.LEFT);
        memberRepository.save(member);
        notifyTeacherAboutMembership(member.getClassroom(), studentId, "đã rời khỏi lớp", "STUDENT_LEFT");
        log.info("Student {} left classroom {}", studentId, classroomId);
    }

    private void notifyTeacherAboutMembership(Classroom classroom, Long studentId, String action, String type) {
        UserDto student = userServiceClient.findById(studentId).orElse(null);
        String studentName = student != null ? student.getUsername() : "Một học sinh";
        notificationClient.notifyUser(classroom.getTeacherId(), studentId, studentName, type,
                studentName + " " + action,
                "Lớp " + classroom.getName(),
                "/classrooms/" + classroom.getId() + "?tab=people",
                "CLASSROOM", classroom.getId());
    }

    private void addOrReactivateMember(Classroom classroom, Long studentId, JoinType joinType) {
        Optional<ClassroomMember> existingMember = memberRepository
                .findByClassroomIdAndStudentId(classroom.getId(), studentId);

        if (existingMember.isPresent()) {
            ClassroomMember member = existingMember.get();
            member.setStatus(MemberStatus.ACTIVE);
            member.setJoinType(joinType);
            member.setJoinedAt(LocalDateTime.now());
            memberRepository.save(member);
        } else {
            ClassroomMember member = ClassroomMember.builder()
                    .classroom(classroom)
                    .studentId(studentId)
                    .joinType(joinType)
                    .status(MemberStatus.ACTIVE)
                    .build();
            memberRepository.save(member);
        }
    }


    private void validateNotAlreadyMember(Long classroomId, Long studentId) {
        if (memberRepository.existsByClassroomIdAndStudentIdAndStatus(
                classroomId, studentId, MemberStatus.ACTIVE)) {
            throw new BusinessException("Bạn đã là thành viên của lớp này");
        }
    }
}
