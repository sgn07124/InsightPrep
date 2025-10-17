package com.project.InsightPrep.domain.question.repository;

import com.project.InsightPrep.domain.question.entity.Answer;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    @Query("""
        SELECT a
        FROM Answer a
        JOIN FETCH a.question q
        JOIN FETCH AnswerFeedback f ON f.answer = a
        WHERE a.member.id = :memberId
        ORDER BY a.id DESC
    """)
    List<Answer> findAllWithQuestionAndFeedbackByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    int countByQuestionId(Long questionId);

    Optional<Answer> findByIdAndMemberId(Long answerId, Long memberId);

    boolean existsByIdAndMemberId(Long answerId, Long memberId);
}
