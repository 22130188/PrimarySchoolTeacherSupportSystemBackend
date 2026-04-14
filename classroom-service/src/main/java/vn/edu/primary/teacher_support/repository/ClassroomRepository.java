package vn.edu.primary.teacher_support.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.primary.teacher_support.entity.Classroom;

import java.util.List;
import java.util.Optional;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

    List<Classroom> findByTeacherIdAndIsDeletedFalseOrderByCreatedAtDesc(Long teacherId);

    Optional<Classroom> findByClassCodeAndIsDeletedFalse(String classCode);

    Optional<Classroom> findByInviteLinkTokenAndIsDeletedFalse(String inviteLinkToken);

    Optional<Classroom> findByIdAndIsDeletedFalse(Long id);

    boolean existsByClassCode(String classCode);

    boolean existsByInviteLinkToken(String inviteLinkToken);

    List<Classroom> findByIsDeletedFalseOrderByCreatedAtDesc();

    List<Classroom> findAllByOrderByCreatedAtDesc();

    List<Classroom> findTop5ByIsDeletedFalseOrderByCreatedAtDesc();

    long countByIsDeletedFalse();

    long countByIsDeletedTrue();
}
