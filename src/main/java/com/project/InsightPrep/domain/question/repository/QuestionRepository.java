package com.project.InsightPrep.domain.question.repository;

import com.project.InsightPrep.domain.question.entity.AnswerStatus;
import com.project.InsightPrep.domain.question.entity.Question;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Transactional
    void deleteByStatusAndCreatedAtBefore(AnswerStatus status, LocalDateTime cutoff);
}
