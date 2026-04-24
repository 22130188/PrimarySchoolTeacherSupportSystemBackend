package vn.edu.primary.teacher_support.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.primary.teacher_support.entity.LessonDraft;

import java.util.List;
import java.util.Optional;

public interface LessonDraftRepository extends JpaRepository<LessonDraft, Long> {
    List<LessonDraft> findByUserIdOrderByUpdatedAtDesc(Long userId);
    Optional<LessonDraft> findByIdAndUserId(Long id, Long userId);
}
