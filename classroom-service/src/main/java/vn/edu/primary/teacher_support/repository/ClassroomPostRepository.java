package vn.edu.primary.teacher_support.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.primary.teacher_support.entity.ClassroomPost;

import java.util.List;
import java.util.Optional;

public interface ClassroomPostRepository extends JpaRepository<ClassroomPost, Long> {

    List<ClassroomPost> findByClassroomIdOrderByCreatedAtDesc(Long classroomId, Pageable pageable);

    Optional<ClassroomPost> findByIdAndClassroomId(Long id, Long classroomId);
}
