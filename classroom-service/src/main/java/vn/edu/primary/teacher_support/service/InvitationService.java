package vn.edu.primary.teacher_support.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.primary.teacher_support.dto.*;
import vn.edu.primary.teacher_support.entity.Classroom;
import vn.edu.primary.teacher_support.entity.ClassroomInvitation;
import vn.edu.primary.teacher_support.entity.enums.InvitationStatus;
import vn.edu.primary.teacher_support.entity.enums.MemberStatus;
import vn.edu.primary.teacher_support.exception.BusinessException;
import vn.edu.primary.teacher_support.exception.ResourceNotFoundException;
import vn.edu.primary.teacher_support.repository.ClassroomInvitationRepository;
import vn.edu.primary.teacher_support.repository.ClassroomMemberRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvitationService {

    private final ClassroomInvitationRepository invitationRepository;
    private final ClassroomMemberRepository memberRepository;
    private final ClassroomService classroomService;
    private final UserServiceClient userServiceClient;
    private final EmailService emailService;
    private final NotificationClient notificationClient;
    private final ActionLogClient actionLogClient;

    @Value("${classroom.invite.expiry-days}")
    private int expiryDays;

    @Transactional
    public InvitationResponse inviteByEmail(Long classroomId, String email, Long invitedBy) {
        email = email.trim().toLowerCase();
        Classroom classroom = classroomService.requireWritableClassroom(classroomId);

        Optional<UserDto> existingUser = userServiceClient.findByEmail(email);

        if (existingUser.isPresent()) {
            UserDto user = existingUser.get();

            if (!"STUDENT".equalsIgnoreCase(user.getRole())) {
                throw new BusinessException("Email " + email + " không thuộc tài khoản học sinh");
            }

            if (memberRepository.existsByClassroomIdAndStudentIdAndStatus(
                    classroomId, user.getId(), MemberStatus.ACTIVE)) {
                throw new BusinessException("Học sinh đã là thành viên của lớp");
            }

            List<ClassroomInvitation> existing = invitationRepository.findActiveInvitations(
                    classroomId, email, List.of(InvitationStatus.INVITED));
            if (!existing.isEmpty()) {
                throw new BusinessException("Đã có lời mời đang hoạt động cho email này");
            }

            ClassroomInvitation invitation = createInvitation(classroom, email, user.getId(), invitedBy,
                    InvitationStatus.INVITED);

            UserDto teacher = userServiceClient.findById(invitedBy).orElse(null);
            String teacherName = teacher != null ? teacher.getUsername() : "Giáo viên";
            emailService.sendInvitationEmail(email, classroom.getName(), teacherName, invitation.getToken());
            notificationClient.notifyUser(user.getId(), invitedBy, teacherName, "CLASS_INVITATION",
                    "Bạn được mời vào lớp " + classroom.getName(),
                    teacherName + " đã gửi cho bạn một lời mời tham gia lớp học.",
                    "/classrooms", "CLASSROOM", classroomId);

            logInviteAction("INVITE_CLASSROOM_MEMBER", classroom, invitedBy, teacherName, email, invitation.getId(), "POST");
            return toResponse(invitation, classroom.getName());
        } else {

            List<ClassroomInvitation> existing = invitationRepository.findActiveInvitations(
                    classroomId, email, List.of(InvitationStatus.WAITING_REGISTER, InvitationStatus.INVITED));
            if (!existing.isEmpty()) {
                throw new BusinessException("Đã có lời mời đang hoạt động cho email này");
            }

            ClassroomInvitation invitation = createInvitation(classroom, email, null, invitedBy,
                    InvitationStatus.WAITING_REGISTER);

            UserDto teacher = userServiceClient.findById(invitedBy).orElse(null);
            String teacherName = teacher != null ? teacher.getUsername() : "Giáo viên";
            emailService.sendRegistrationInviteEmail(email, classroom.getName(), teacherName, invitation.getToken());

            logInviteAction("INVITE_CLASSROOM_MEMBER", classroom, invitedBy, teacherName, email, invitation.getId(), "POST");
            return toResponse(invitation, classroom.getName());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String inviteByEmailForBatch(Long classroomId, Classroom classroom, String email, Long invitedBy) {
        classroomService.ensureWritable(classroom);
        email = email.trim().toLowerCase();

        Optional<UserDto> existingUser = userServiceClient.findByEmail(email);

        if (existingUser.isPresent()) {
            UserDto user = existingUser.get();

            if (!"STUDENT".equalsIgnoreCase(user.getRole())) {
                return "invalid_role";
            }

            if (memberRepository.existsByClassroomIdAndStudentIdAndStatus(
                    classroomId, user.getId(), MemberStatus.ACTIVE)) {
                return "already_member";
            }

            List<ClassroomInvitation> existing = invitationRepository.findActiveInvitations(
                    classroomId, email, List.of(InvitationStatus.INVITED));
            if (!existing.isEmpty()) {
                return "already_invited";
            }

            ClassroomInvitation invitation = createInvitation(classroom, email, user.getId(), invitedBy,
                    InvitationStatus.INVITED);

            UserDto teacher = userServiceClient.findById(invitedBy).orElse(null);
            String teacherName = teacher != null ? teacher.getUsername() : "Giáo viên";
            emailService.sendInvitationEmail(email, classroom.getName(), teacherName, invitation.getToken());
            notificationClient.notifyUser(user.getId(), invitedBy, teacherName, "CLASS_INVITATION",
                    "Bạn được mời vào lớp " + classroom.getName(),
                    teacherName + " đã gửi cho bạn một lời mời tham gia lớp học.",
                    "/classrooms", "CLASSROOM", classroomId);

            return "invited_success";
        } else {
            List<ClassroomInvitation> existing = invitationRepository.findActiveInvitations(
                    classroomId, email, List.of(InvitationStatus.WAITING_REGISTER, InvitationStatus.INVITED));
            if (!existing.isEmpty()) {
                return "already_invited";
            }

            ClassroomInvitation invitation = createInvitation(classroom, email, null, invitedBy,
                    InvitationStatus.WAITING_REGISTER);

            UserDto teacher = userServiceClient.findById(invitedBy).orElse(null);
            String teacherName = teacher != null ? teacher.getUsername() : "Giáo viên";
            emailService.sendRegistrationInviteEmail(email, classroom.getName(), teacherName, invitation.getToken());

            return "waiting_register";
        }
    }


    @Transactional
    public void resendInvitation(Long classroomId, Long invitationId, Long teacherId) {
        Classroom classroom = classroomService.requireWritableClassroom(classroomId);

        ClassroomInvitation invitation = invitationRepository.findByIdAndClassroomId(invitationId, classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lời mời"));

        if (invitation.getStatus() != InvitationStatus.INVITED
                && invitation.getStatus() != InvitationStatus.WAITING_REGISTER) {
            throw new BusinessException("Không thể gửi lại lời mời đã " + invitation.getStatus().name());
        }

        invitation.setExpiredAt(LocalDateTime.now().plusDays(expiryDays));
        invitation.setToken(UUID.randomUUID().toString().replace("-", ""));
        invitationRepository.save(invitation);

        UserDto teacher = userServiceClient.findById(teacherId).orElse(null);
        String teacherName = teacher != null ? teacher.getUsername() : "Giáo viên";

        if (invitation.getStatus() == InvitationStatus.INVITED) {
            emailService.sendInvitationEmail(invitation.getEmail(), classroom.getName(), teacherName,
                    invitation.getToken());
            notificationClient.notifyUser(invitation.getStudentId(), teacherId, teacherName,
                    "CLASS_INVITATION", "Lời mời lớp học đã được gửi lại",
                    "Lời mời tham gia lớp " + classroom.getName() + " vừa được gia hạn.",
                    "/classrooms", "CLASSROOM", classroomId);
        } else {
            emailService.sendRegistrationInviteEmail(invitation.getEmail(), classroom.getName(), teacherName,
                    invitation.getToken());
        }
        logInviteAction("RESEND_CLASSROOM_INVITATION", classroom, teacherId, teacherName,
                invitation.getEmail(), invitationId, "POST");
    }


    @Transactional
    public void revokeInvitation(Long classroomId, Long invitationId, Long teacherId) {
        ClassroomInvitation invitation = invitationRepository.findByIdAndClassroomId(invitationId, classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lời mời"));
        classroomService.ensureWritable(invitation.getClassroom());

        if (invitation.getStatus() == InvitationStatus.ACCEPTED) {
            throw new BusinessException("Không thể thu hồi lời mời đã được chấp nhận");
        }

        invitation.setStatus(InvitationStatus.CANCELLED);
        invitationRepository.save(invitation);
        Classroom classroom = invitation.getClassroom();
        UserDto teacher = userServiceClient.findById(teacherId).orElse(null);
        String teacherName = teacher != null ? teacher.getUsername() : "Giáo viên";
        if (invitation.getStudentId() != null) {
            notificationClient.notifyUser(invitation.getStudentId(), teacherId, "Giáo viên",
                    "INVITATION_REVOKED", "Lời mời tham gia lớp đã được thu hồi",
                    "Lớp " + (classroom != null ? classroom.getName() : ""),
                    "/classrooms", "CLASSROOM", classroomId);
        }
        if (classroom != null) {
            logInviteAction("REVOKE_CLASSROOM_INVITATION", classroom, teacherId, teacherName,
                    invitation.getEmail(), invitationId, "DELETE");
        }
    }

    private void logInviteAction(String action, Classroom classroom, Long actorId, String actorName,
                                 String email, Long invitationId, String httpMethod) {
        String safeName = classroom.getName() == null ? "" : classroom.getName().replace("\\", "\\\\").replace("\"", "\\\"");
        String safeEmail = email == null ? "" : email.replace("\\", "\\\\").replace("\"", "\\\"");
        actionLogClient.log(
                actorName,
                action,
                "classrooms",
                invitationId == null ? String.valueOf(classroom.getId()) : String.valueOf(invitationId),
                httpMethod,
                "/api/classrooms/" + classroom.getId() + "/invite",
                "WARNING",
                "SUCCESS",
                "{\"classroomId\":" + classroom.getId()
                        + ",\"classroomName\":\"" + safeName
                        + "\",\"email\":\"" + safeEmail + "\"}"
        );
    }


    public List<InvitationResponse> getInvitations(Long classroomId) {
        Classroom classroom = classroomService.getActiveClassroom(classroomId);
        List<ClassroomInvitation> invitations = invitationRepository
                .findByClassroomIdOrderByCreatedAtDesc(classroomId);

        return invitations.stream()
                .map(inv -> toResponse(inv, classroom.getName()))
                .collect(Collectors.toList());
    }


    public List<InvitationResponse> getMyInvitations(Long studentId, String email) {
        List<ClassroomInvitation> invitations = invitationRepository
                .findByEmailAndStatusIn(email, List.of(InvitationStatus.INVITED));

        return invitations.stream()
                .map(inv -> toResponse(inv, inv.getClassroom().getName()))
                .collect(Collectors.toList());
    }


    @Transactional
    public void acceptInvitation(Long invitationId, Long studentId, String studentEmail) {
        ClassroomInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lời mời"));

        if (!invitation.getEmail().equalsIgnoreCase(studentEmail)) {
            throw new BusinessException("Lời mời không dành cho tài khoản này");
        }

        if (invitation.getStatus() != InvitationStatus.INVITED) {
            if (invitation.getStatus() == InvitationStatus.ACCEPTED) {
                return;
            }
            throw new BusinessException("Lời mời không hợp lệ (trạng thái: " + invitation.getStatus() + ")");
        }

        if (LocalDateTime.now().isAfter(invitation.getExpiredAt())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
            throw new BusinessException("Lời mời đã hết hạn");
        }

        Classroom classroom = invitation.getClassroom();
        classroomService.ensureWritable(classroom);
        if (classroom.getIsDeleted()) {
            throw new BusinessException("Lớp học đã bị xóa");
        }

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(LocalDateTime.now());
        invitation.setStudentId(studentId);
        invitationRepository.save(invitation);
    }


    @Transactional
    public void rejectInvitation(Long invitationId, Long studentId, String studentEmail) {
        ClassroomInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lời mời"));
        classroomService.ensureWritable(invitation.getClassroom());

        if (!invitation.getEmail().equalsIgnoreCase(studentEmail)) {
            throw new BusinessException("Lời mời không dành cho tài khoản này");
        }

        if (invitation.getStatus() != InvitationStatus.INVITED) {
            throw new BusinessException("Không thể từ chối lời mời ở trạng thái: " + invitation.getStatus());
        }

        invitation.setStatus(InvitationStatus.REJECTED);
        invitation.setRejectedAt(LocalDateTime.now());
        invitationRepository.save(invitation);

        UserDto student = userServiceClient.findById(studentId).orElse(null);
        String studentName = student != null ? student.getUsername() : "Học sinh";
        notificationClient.notifyUser(invitation.getInvitedBy(), studentId, studentName,
                "INVITATION_REJECTED", studentName + " đã từ chối lời mời",
                "Lời mời tham gia lớp " + invitation.getClassroom().getName() + " đã bị từ chối.",
                "/classrooms/" + invitation.getClassroom().getId() + "?tab=people",
                "CLASSROOM", invitation.getClassroom().getId());
    }


    public InvitationResponse getByToken(String token) {
        ClassroomInvitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lời mời"));
        return toResponse(invitation, invitation.getClassroom().getName());
    }


    @Transactional
    public int resolveAfterRegister(String email, Long userId) {
        email = email.trim().toLowerCase();
        List<ClassroomInvitation> waitingInvitations = invitationRepository
                .findByEmailAndStatus(email, InvitationStatus.WAITING_REGISTER);

        int count = 0;
        for (ClassroomInvitation invitation : waitingInvitations) {
            if (!classroomService.isWritable(invitation.getClassroom())) continue;
            invitation.setStudentId(userId);
            invitation.setStatus(InvitationStatus.INVITED);
            invitationRepository.save(invitation);
            UserDto teacher = userServiceClient.findById(invitation.getInvitedBy()).orElse(null);
            String teacherName = teacher != null ? teacher.getUsername() : "Giáo viên";
            notificationClient.notifyUser(userId, invitation.getInvitedBy(), teacherName,
                    "CLASS_INVITATION", "Bạn được mời vào lớp " + invitation.getClassroom().getName(),
                    teacherName + " đã gửi cho bạn một lời mời tham gia lớp học.",
                    "/classrooms", "CLASSROOM", invitation.getClassroom().getId());
            count++;
        }

        log.info("Resolved {} WAITING_REGISTER invitations for email {} (userId={})", count, email, userId);
        return count;
    }


    private ClassroomInvitation createInvitation(Classroom classroom, String email, Long studentId,
            Long invitedBy, InvitationStatus status) {
        ClassroomInvitation invitation = ClassroomInvitation.builder()
                .classroom(classroom)
                .email(email)
                .studentId(studentId)
                .invitedBy(invitedBy)
                .token(UUID.randomUUID().toString().replace("-", ""))
                .status(status)
                .expiredAt(LocalDateTime.now().plusDays(expiryDays))
                .build();

        return invitationRepository.save(invitation);
    }

    private InvitationResponse toResponse(ClassroomInvitation inv, String classroomName) {
        String studentName = null;
        if (inv.getStudentId() != null) {
            UserDto u = userServiceClient.findById(inv.getStudentId()).orElse(null);
            studentName = u != null ? u.getUsername() : null;
        }

        return InvitationResponse.builder()
                .id(inv.getId())
                .classroomId(inv.getClassroom().getId())
                .classroomName(classroomName)
                .email(inv.getEmail())
                .studentId(inv.getStudentId())
                .studentName(studentName)
                .status(inv.getStatus().name())
                .token(inv.getToken())
                .invitedAt(inv.getCreatedAt())
                .expiredAt(inv.getExpiredAt())
                .acceptedAt(inv.getAcceptedAt())
                .rejectedAt(inv.getRejectedAt())
                .build();
    }
}
