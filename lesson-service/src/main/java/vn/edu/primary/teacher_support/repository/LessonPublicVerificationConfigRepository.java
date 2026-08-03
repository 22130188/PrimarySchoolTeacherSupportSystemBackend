package vn.edu.primary.teacher_support.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.primary.teacher_support.entity.LessonPublicVerificationConfig;

public interface LessonPublicVerificationConfigRepository extends JpaRepository<LessonPublicVerificationConfig, Long> {
}
