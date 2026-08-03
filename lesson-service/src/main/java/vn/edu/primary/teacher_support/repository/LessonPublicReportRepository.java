package vn.edu.primary.teacher_support.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.primary.teacher_support.entity.LessonPublicReport;
import vn.edu.primary.teacher_support.entity.enums.PublicReportStatus;

import java.util.List;
import java.util.Optional;

public interface LessonPublicReportRepository extends JpaRepository<LessonPublicReport, Long> {
    long countByDraftIdAndStatus(Long draftId, PublicReportStatus status);

    List<LessonPublicReport> findByStatusOrderByCreatedAtDesc(PublicReportStatus status);

    List<LessonPublicReport> findAllByOrderByCreatedAtDesc();

    List<LessonPublicReport> findByDraftIdOrderByCreatedAtDesc(Long draftId);

    Optional<LessonPublicReport> findByDraftIdAndReporterIdAndStatus(Long draftId, Long reporterId, PublicReportStatus status);
}
