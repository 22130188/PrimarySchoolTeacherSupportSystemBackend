package vn.edu.primary.test.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.primary.test.entity.TestAttempt;

import java.util.List;

@Repository
public interface TestAttemptRepository extends JpaRepository<TestAttempt, Long> {
    List<TestAttempt> findByTest_IdOrderByCreatedAtDesc(Long testId);
    List<TestAttempt> findByTest_IdAndUserIdOrderByCreatedAtDesc(Long testId, Long userId);
    int countByTest_IdAndUserIdAndIsSubmittedTrue(Long testId, Long userId);
}
