package vn.edu.primary.teacher_support.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.primary.teacher_support.entity.LessonTemplate;
import vn.edu.primary.teacher_support.entity.enums.LessonTemplateStatus;

import java.util.List;

public interface LessonTemplateRepository extends JpaRepository<LessonTemplate, Long> {
    List<LessonTemplate> findAllByOrderByUpdatedAtDesc();

    @Query("SELECT t FROM LessonTemplate t WHERE t.status = :status " +
            "AND (:subject IS NULL OR t.subject = :subject) " +
            "AND (:grade IS NULL OR t.grade = :grade) " +
            "AND (:type IS NULL OR t.type = :type) " +
            "ORDER BY t.updatedAt DESC")
    List<LessonTemplate> findVisibleTemplates(
            @Param("status") LessonTemplateStatus status,
            @Param("subject") String subject,
            @Param("grade") String grade,
            @Param("type") String type
    );
}
