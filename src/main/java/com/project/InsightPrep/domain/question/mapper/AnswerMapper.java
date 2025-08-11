package com.project.InsightPrep.domain.question.mapper;

import com.project.InsightPrep.domain.question.dto.response.QuestionResponse.QuestionsDto;
import com.project.InsightPrep.domain.question.entity.Answer;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AnswerMapper {
    void insertAnswer(Answer answer);

    List<QuestionsDto> findQuestionsWithFeedback(long memberId);
}