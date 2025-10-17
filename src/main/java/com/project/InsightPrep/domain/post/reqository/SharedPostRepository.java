package com.project.InsightPrep.domain.post.reqository;

import com.project.InsightPrep.domain.post.entity.SharedPost;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SharedPostRepository extends JpaRepository<SharedPost, Long> {

    @EntityGraph(attributePaths = {
            "member", "answer", "answer.question", "answer.feedback"
    })
    Optional<SharedPost> findById(Long id);  // findPostDetailById
}
