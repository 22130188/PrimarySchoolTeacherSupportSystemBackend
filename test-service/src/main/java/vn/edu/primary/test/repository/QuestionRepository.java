package vn.edu.primary.test.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.primary.test.entity.Question;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByTestIdOrderByOrderIndexAsc(Long testId);
    List<Question> findByTest_CreatedByOrderByIdDesc(Long userId);
    
    @Query("SELECT q FROM Question q WHERE q.test.createdBy != :userId ORDER BY q.id DESC")
    List<Question> findByTest_CreatedByNotOrderByIdDesc(Long userId);
    
    @Query("SELECT q FROM Question q ORDER BY q.id DESC")
    List<Question> findAllOrderByIdDesc();
    
    @Query("SELECT q FROM Question q WHERE q.test.subject = :subject AND q.test.createdBy = :userId ORDER BY q.id DESC")
    List<Question> findBySubjectAndCreatedByOrderByIdDesc(String subject, Long userId);
    
    @Query("SELECT q FROM Question q WHERE q.test.lessonContentName = :lessonContent AND q.test.createdBy = :userId ORDER BY q.id DESC")
    List<Question> findByLessonContentAndCreatedByOrderByIdDesc(String lessonContent, Long userId);
}
