package com.project.InsightPrep.domain.post.reqository;

import com.project.InsightPrep.domain.post.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    long countBySharedPostId(Long postId);
}
