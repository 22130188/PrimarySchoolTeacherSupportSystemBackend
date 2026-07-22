package vn.edu.primary.teacher_support.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.primary.teacher_support.entity.LessonPublicRating;

import java.util.Optional;

public interface LessonPublicRatingRepository extends JpaRepository<LessonPublicRating, Long> {
    Optional<LessonPublicRating> findByDraftIdAndUserId(Long draftId, Long userId);

    long countByDraftId(Long draftId);

    @Query("SELECT COALESCE(AVG(r.stars), 0) FROM LessonPublicRating r WHERE r.draftId = :draftId")
    Double averageStarsByDraftId(@Param("draftId") Long draftId);
}
