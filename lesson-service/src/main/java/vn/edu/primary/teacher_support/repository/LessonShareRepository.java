package vn.edu.primary.teacher_support.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.primary.teacher_support.entity.LessonShare;

import java.util.List;
import java.util.Optional;

public interface LessonShareRepository extends JpaRepository<LessonShare, Long> {

    List<LessonShare> findByDraftIdOrderByCreatedAtDesc(Long draftId);

    List<LessonShare> findBySharedWithUserIdOrderByCreatedAtDesc(Long sharedWithUserId);

    Optional<LessonShare> findByDraftIdAndSharedWithUserId(Long draftId, Long sharedWithUserId);

    void deleteByDraftIdAndSharedWithUserId(Long draftId, Long sharedWithUserId);

    void deleteByDraftId(Long draftId);
}
