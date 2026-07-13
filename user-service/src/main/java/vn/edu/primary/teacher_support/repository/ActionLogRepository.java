package vn.edu.primary.teacher_support.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import vn.edu.primary.teacher_support.entity.ActionLog;

import java.time.LocalDateTime;
import java.util.List;

public interface ActionLogRepository extends JpaRepository<ActionLog, Long>, JpaSpecificationExecutor<ActionLog> {
    List<ActionLog> findTop8ByOrderByCreatedAtDesc();
    List<ActionLog> findTop30ByOrderByCreatedAtDesc();
    long countByStatusAndCreatedAtBetween(String status, LocalDateTime from, LocalDateTime to);
}
