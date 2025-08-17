package com.project.InsightPrep.domain.question.service;

import com.project.InsightPrep.domain.question.dto.request.AnswerRequest.AnswerDto;
import com.project.InsightPrep.domain.question.dto.response.AnswerResponse;

public interface AnswerService {

    AnswerResponse.AnswerDto saveAnswer(AnswerDto dto, Long questionId);

    void deleteAnswer(long answerId);
}