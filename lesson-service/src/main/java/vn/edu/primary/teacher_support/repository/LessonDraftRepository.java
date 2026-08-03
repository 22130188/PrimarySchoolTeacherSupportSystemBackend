package vn.edu.primary.teacher_support.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.primary.teacher_support.entity.LessonDraft;
import vn.edu.primary.teacher_support.entity.enums.PublicVerificationStatus;

import java.util.List;
import java.util.Optional;

public interface LessonDraftRepository extends JpaRepository<LessonDraft, Long> {
    List<LessonDraft> findByUserIdOrderByUpdatedAtDesc(Long userId);
    List<LessonDraft> findAllByOrderByUpdatedAtDesc();
    Optional<LessonDraft> findByIdAndUserId(Long id, Long userId);

    List<LessonDraft> findByIsPublicTrueOrderByPublicPublishedAtDesc();

    @Query("SELECT d FROM LessonDraft d WHERE d.isPublic = true " +
           "AND (:subject IS NULL OR d.subject = :subject) " +
           "AND (:grade IS NULL OR d.grade = :grade) " +
           "AND (:type IS NULL OR d.type = :type) " +
           "AND (:verificationStatus IS NULL OR d.publicVerificationStatus = :verificationStatus) " +
           "AND (:keyword IS NULL OR LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY d.publicPublishedAt DESC")
    List<LessonDraft> searchPublicDrafts(@Param("subject") String subject,
                                         @Param("grade") String grade,
                                         @Param("type") String type,
                                         @Param("keyword") String keyword,
                                         @Param("verificationStatus") PublicVerificationStatus verificationStatus);

    @Query("SELECT d FROM LessonDraft d WHERE d.userId = :userId " +
           "AND (:title IS NULL OR LOWER(d.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
           "AND (:subject IS NULL OR d.subject = :subject) " +
           "AND (:grade IS NULL OR d.grade = :grade) " +
           "ORDER BY d.updatedAt DESC")
    List<LessonDraft> searchDrafts(@Param("userId") Long userId,
                                    @Param("title") String title,
                                    @Param("subject") String subject,
                                    @Param("grade") String grade);
}
