package com.project.InsightPrep.domain.question.service;

import com.project.InsightPrep.domain.question.dto.response.QuestionResponse;
import com.project.InsightPrep.domain.question.dto.response.QuestionResponse.QuestionsDto;
import java.util.List;

public interface QuestionService {

    QuestionResponse.QuestionDto createQuestion(String category);

    List<QuestionsDto> getQuestions();
}
