package com.project.InsightPrep.domain.question.mapper;

import com.project.InsightPrep.domain.question.dto.response.QuestionResponse.QuestionsDto;
import com.project.InsightPrep.domain.question.entity.Answer;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AnswerMapper {
    void insertAnswer(Answer answer);

    List<QuestionsDto> findQuestionsWithFeedback(long memberId);

    List<QuestionsDto> findQuestionsWithFeedbackPaged(@Param("memberId") long memberId, @Param("limit") int limit, @Param("offset") int offset);

    long countQuestionsWithFeedback(@Param("memberId") long memberId);

    Long findQuestionIdOfMyAnswer(@Param("answerId") long answerId, @Param("memberId") long memberId);

    int deleteMyAnswerById(@Param("answerId") long answerId, @Param("memberId") long memberId);

    void resetQuestionStatusIfNoAnswers(@Param("questionId") Long questionId, @Param("waiting") String waitingStatus);
}