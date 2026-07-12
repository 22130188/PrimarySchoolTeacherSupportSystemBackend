package vn.edu.primary.teacher_support.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.primary.teacher_support.entity.Guide;
import java.util.List;
import java.util.Optional;

public interface GuideRepository extends JpaRepository<Guide, Long> {
    List<Guide> findByPublishedTrueOrderBySortOrderAsc();
    List<Guide> findAllByOrderBySortOrderAsc();
    Optional<Guide> findBySlug(String slug);
}
