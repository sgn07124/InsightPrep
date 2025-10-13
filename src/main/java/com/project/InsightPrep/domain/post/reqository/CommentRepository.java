package com.project.InsightPrep.domain.post.reqository;

import com.project.InsightPrep.domain.post.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    long countBySharedPostId(Long postId);

    Page<Comment> findBySharedPost_IdOrderByCreatedAtAscIdAsc(Long postId, Pageable pageable);

    long countBySharedPost_Id(Long postId);
}
