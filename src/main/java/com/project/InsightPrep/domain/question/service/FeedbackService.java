package com.project.InsightPrep.domain.question.service;

import com.project.InsightPrep.domain.question.dto.response.AnswerResponse.FeedbackDto;
import com.project.InsightPrep.domain.question.entity.Answer;

public interface FeedbackService {
    void saveFeedback(Answer answer);

    FeedbackDto getFeedback(long answerId);
}
