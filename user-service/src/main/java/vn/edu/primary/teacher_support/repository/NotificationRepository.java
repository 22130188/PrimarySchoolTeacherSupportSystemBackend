package vn.edu.primary.teacher_support.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.primary.teacher_support.entity.Notification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findTop100ByTargetUserIdOrderByCreatedAtDesc(Long targetUserId);

    long countByTargetUserIdAndReadAtIsNull(Long targetUserId);

    Optional<Notification> findByIdAndTargetUserId(Long id, Long targetUserId);

    @Modifying
    @Query("UPDATE Notification n SET n.readAt = :readAt WHERE n.targetUserId = :userId AND n.readAt IS NULL")
    int markAllRead(@Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);
}
