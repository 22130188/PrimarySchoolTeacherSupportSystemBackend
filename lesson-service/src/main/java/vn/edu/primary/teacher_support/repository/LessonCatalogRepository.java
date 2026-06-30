package vn.edu.primary.teacher_support.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.primary.teacher_support.entity.LessonCatalog;

import java.util.List;

public interface LessonCatalogRepository extends JpaRepository<LessonCatalog, Long> {

    @Query("SELECT c FROM LessonCatalog c WHERE " +
            "(:activeOnly = false OR c.isActive = true) " +
            "AND (:subject IS NULL OR c.subject = :subject) " +
            "AND (:grade IS NULL OR c.grade = :grade) " +
            "AND (:volume IS NULL OR c.volume = :volume) " +
            "AND (:book IS NULL OR c.book = :book) " +
            "ORDER BY c.subject ASC, c.grade ASC, c.volume ASC, c.book ASC, c.name ASC")
    List<LessonCatalog> searchCatalog(@Param("subject") String subject,
                                      @Param("grade") String grade,
                                      @Param("volume") String volume,
                                      @Param("book") String book,
                                      @Param("activeOnly") boolean activeOnly);
}