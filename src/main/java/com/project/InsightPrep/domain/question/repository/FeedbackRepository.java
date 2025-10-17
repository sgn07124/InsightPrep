package com.project.InsightPrep.domain.question.repository;

import com.project.InsightPrep.domain.question.entity.AnswerFeedback;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<AnswerFeedback, Long> {

    @EntityGraph(attributePaths = {"answer", "answer.question"})
    Optional<AnswerFeedback> findByAnswerId(Long answerId);
}
