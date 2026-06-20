package vn.edu.primary.teacher_support.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.primary.teacher_support.entity.PostComment;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    List<PostComment> findByPostIdOrderByCreatedAtAsc(Long postId);

    long countByPostId(Long postId);

    void deleteAllByPostId(Long postId);
}
