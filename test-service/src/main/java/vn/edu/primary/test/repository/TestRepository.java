package vn.edu.primary.test.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.primary.test.entity.Test;
import vn.edu.primary.test.entity.TestStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestRepository extends JpaRepository<Test, Long> {
    List<Test> findByCreatedByOrderByCreatedAtDesc(Long userId);
    List<Test> findByCreatedByAndStatusOrderByCreatedAtDesc(Long userId, TestStatus status);
    Optional<Test> findByIdAndCreatedBy(Long id, Long userId);
}
