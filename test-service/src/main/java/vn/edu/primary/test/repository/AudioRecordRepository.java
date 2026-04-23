package vn.edu.primary.test.repository;

import vn.edu.primary.test.entity.AudioRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AudioRecordRepository extends JpaRepository<AudioRecord, Long> {
    List<AudioRecord> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<AudioRecord> findAllByOrderByCreatedAtDesc();
    void deleteByUserId(Long userId);
}

