package vn.edu.primary.teacher_support.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.primary.teacher_support.entity.LessonClassroomShare;

import java.util.List;
import java.util.Optional;

public interface LessonClassroomShareRepository extends JpaRepository<LessonClassroomShare, Long> {

    List<LessonClassroomShare> findByDraftIdOrderByCreatedAtDesc(Long draftId);

    List<LessonClassroomShare> findByClassroomIdOrderByCreatedAtDesc(Long classroomId);

    Optional<LessonClassroomShare> findByDraftIdAndClassroomId(Long draftId, Long classroomId);

    void deleteByDraftIdAndClassroomId(Long draftId, Long classroomId);

    void deleteByDraftId(Long draftId);
}
