package vn.edu.primary.teacher_support.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.primary.teacher_support.entity.Book;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {
    @EntityGraph(attributePaths = {"pages"})
    Optional<Book> findBySlugId(String slugId);

    @Query("SELECT b FROM Book b WHERE " +
           "(:grade IS NULL OR b.grade = :grade) AND " +
           "(:subject IS NULL OR LOWER(b.subject) = LOWER(:subject)) AND " +
           "(:bookType IS NULL OR LOWER(b.bookType) = LOWER(:bookType)) AND " +
           "(:search IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Book> findBooks(@Param("grade") Integer grade, 
                         @Param("subject") String subject, 
                         @Param("bookType") String bookType,
                         @Param("search") String search);
}
