package vn.edu.primary.teacher_support.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.primary.teacher_support.entity.LessonComment;

import java.util.List;

public interface LessonCommentRepository extends JpaRepository<LessonComment, Long> {

    List<LessonComment> findByClassroomShareIdOrderByCreatedAtAsc(Long classroomShareId);

    long countByClassroomShareId(Long classroomShareId);
}
