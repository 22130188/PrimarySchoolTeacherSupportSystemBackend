package vn.edu.primary.teacher_support.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.primary.teacher_support.entity.ClassroomMember;
import vn.edu.primary.teacher_support.entity.enums.MemberStatus;

import java.util.List;
import java.util.Optional;

public interface ClassroomMemberRepository extends JpaRepository<ClassroomMember, Long> {

    boolean existsByClassroomIdAndStudentIdAndStatus(Long classroomId, Long studentId, MemberStatus status);

    Optional<ClassroomMember> findByClassroomIdAndStudentIdAndStatus(Long classroomId, Long studentId, MemberStatus status);

    List<ClassroomMember> findByClassroomIdAndStatusOrderByJoinedAtDesc(Long classroomId, MemberStatus status);

    List<ClassroomMember> findByStudentIdAndStatusOrderByJoinedAtDesc(Long studentId, MemberStatus status);

    Optional<ClassroomMember> findByIdAndClassroomId(Long id, Long classroomId);
}
