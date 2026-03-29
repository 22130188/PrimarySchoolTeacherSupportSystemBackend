package vn.edu.primary.teacher_support.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.primary.teacher_support.entity.TeacherClass;
import vn.edu.primary.teacher_support.entity.User;

public interface TeacherClassRepository extends JpaRepository<TeacherClass, Long> {
    void deleteByUser(User user);
}
