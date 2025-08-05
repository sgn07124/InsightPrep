package com.project.InsightPrep.domain.question.service;

import com.project.InsightPrep.domain.question.dto.response.QuestionResponse;

public interface QuestionService {

    QuestionResponse.QuestionDto createQuestion(String category);
}
