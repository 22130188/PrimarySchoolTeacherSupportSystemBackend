package vn.edu.primary.test.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.primary.test.entity.LessonContent;

import java.util.List;

public interface LessonContentRepository extends JpaRepository<LessonContent, Long> {
    List<LessonContent> findAllByIsActiveTrueOrderBySubjectAscGradeAscNameAsc();
    List<LessonContent> findAllByOrderBySubjectAscGradeAscNameAsc();
}
