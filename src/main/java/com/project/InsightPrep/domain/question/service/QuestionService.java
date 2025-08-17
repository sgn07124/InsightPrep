package com.project.InsightPrep.domain.question.service;

import com.project.InsightPrep.domain.question.dto.response.PageResponse;
import com.project.InsightPrep.domain.question.dto.response.QuestionResponse;
import com.project.InsightPrep.domain.question.dto.response.QuestionResponse.QuestionsDto;

public interface QuestionService {

    QuestionResponse.QuestionDto createQuestion(String category);

    PageResponse<QuestionsDto> getQuestions(int page, int size);
}
