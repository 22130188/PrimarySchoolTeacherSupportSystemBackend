package vn.edu.primary.image.repository;

import vn.edu.primary.image.entity.ImageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ImageRecordRepository extends JpaRepository<ImageRecord, Long> {

    List<ImageRecord> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<ImageRecord> findAllByOrderByCreatedAtDesc();
}