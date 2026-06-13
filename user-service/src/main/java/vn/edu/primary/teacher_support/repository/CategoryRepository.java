package vn.edu.primary.teacher_support.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.primary.teacher_support.entity.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByTypeOrderByCreatedAtDesc(String type);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}
