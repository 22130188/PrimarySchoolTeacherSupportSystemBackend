package vn.edu.primary.teacher_support.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.primary.teacher_support.entity.Classroom;
import vn.edu.primary.teacher_support.entity.enums.ClassroomStatus;

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

    @Query("""
            SELECT c FROM Classroom c
            WHERE c.isDeleted = false
              AND (:status IS NULL OR c.status = :status)
              AND (
                    :keyword = ''
                    OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(c.classCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(c.classDisplayName, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(c.classGroup, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(c.subject, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(c.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            """)
    Page<Classroom> findAdminClassrooms(
            @Param("status") ClassroomStatus status,
            @Param("keyword") String keyword,
            Pageable pageable);
}
