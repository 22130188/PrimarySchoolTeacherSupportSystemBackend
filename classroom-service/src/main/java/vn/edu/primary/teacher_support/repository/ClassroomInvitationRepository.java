package vn.edu.primary.teacher_support.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.primary.teacher_support.entity.ClassroomInvitation;
import vn.edu.primary.teacher_support.entity.enums.InvitationStatus;

import java.util.List;
import java.util.Optional;

public interface ClassroomInvitationRepository extends JpaRepository<ClassroomInvitation, Long> {

    Optional<ClassroomInvitation> findByToken(String token);

    Optional<ClassroomInvitation> findByIdAndClassroomId(Long id, Long classroomId);

    List<ClassroomInvitation> findByClassroomIdOrderByCreatedAtDesc(Long classroomId);

    List<ClassroomInvitation> findByEmailAndStatusIn(String email, List<InvitationStatus> statuses);

    List<ClassroomInvitation> findByStudentIdAndStatusIn(Long studentId, List<InvitationStatus> statuses);

    @Query("""
            SELECT i FROM ClassroomInvitation i
            WHERE i.classroom.id = :classroomId
              AND i.email = :email
              AND i.status IN :statuses
            """)
    List<ClassroomInvitation> findActiveInvitations(
            @Param("classroomId") Long classroomId,
            @Param("email") String email,
            @Param("statuses") List<InvitationStatus> statuses);

    List<ClassroomInvitation> findByEmailAndStatus(String email, InvitationStatus status);

    @Query("""
            SELECT i FROM ClassroomInvitation i
            WHERE i.classroom.id = :classroomId
              AND i.status IN :statuses
            ORDER BY i.createdAt DESC
            """)
    List<ClassroomInvitation> findByClassroomIdAndStatuses(
            @Param("classroomId") Long classroomId,
            @Param("statuses") List<InvitationStatus> statuses);
}
