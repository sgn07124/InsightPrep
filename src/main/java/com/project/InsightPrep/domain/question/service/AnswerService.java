package com.project.InsightPrep.domain.question.service;

import com.project.InsightPrep.domain.question.dto.request.AnswerRequest.AnswerDto;

public interface AnswerService {

    void saveAnswer(AnswerDto dto, Long questionId);

    void deleteAnswer(long answerId);
}